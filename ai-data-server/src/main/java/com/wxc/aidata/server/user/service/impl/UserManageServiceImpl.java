package com.wxc.aidata.server.user.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.auth.service.PasswordService;
import com.wxc.aidata.server.common.id.IdGenerator;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import com.wxc.aidata.server.user.mapper.UserManageMapper;
import com.wxc.aidata.server.user.model.UserCreateCommand;
import com.wxc.aidata.server.user.model.UserPageQuery;
import com.wxc.aidata.server.user.model.UserRoleAssignCommand;
import com.wxc.aidata.server.user.model.UserUpdateCommand;
import com.wxc.aidata.server.user.response.UserResponse;
import com.wxc.aidata.server.user.service.UserManageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现，负责用户 CRUD、角色分配和权限缓存刷新。
 */
@Service
public class UserManageServiceImpl implements UserManageService {

    /**
     * 默认管理员用户 ID，第一版初始化脚本固定为 1。
     */
    private static final Long ADMIN_USER_ID = 1L;

    private static final int USER_ERROR_CODE = 11001;

    private final UserManageMapper userManageMapper;
    private final PasswordService passwordService;
    private final IdGenerator idGenerator;
    private final UserPermissionService userPermissionService;

    /**
     * 注入用户管理、密码、ID 和权限缓存组件。
     */
    public UserManageServiceImpl(
            UserManageMapper userManageMapper,
            PasswordService passwordService,
            IdGenerator idGenerator,
            UserPermissionService userPermissionService) {

        this.userManageMapper = userManageMapper;
        this.passwordService = passwordService;
        this.idGenerator = idGenerator;
        this.userPermissionService = userPermissionService;
    }

    /**
     * 分页查询用户列表，附带当前已绑定角色 ID。
     */
    @Override
    public PageResult<UserResponse> pageUsers(UserPageQuery query) {
        UserPageQuery safeQuery = query == null ? new UserPageQuery(1, 10, null, null, null) : query;
        // PageHelper 负责追加分页 SQL 并执行 count 查询，业务 SQL 不手写 LIMIT/OFFSET。
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<UserManageMapper.UserRow> rows = userManageMapper.findUsers(safeQuery);
        PageInfo<UserManageMapper.UserRow> pageInfo = new PageInfo<>(rows);
        List<UserResponse> records = rows.stream()
                .map(row -> toResponse(row, userManageMapper.findRoleIdsByUserId(row.id())))
                .toList();
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 查询用户详情，用户不存在或已删除时返回业务异常。
     */
    @Override
    public UserResponse getUser(Long id) {
        validateId(id);
        return userManageMapper.findUserById(id)
                .map(row -> toResponse(row, userManageMapper.findRoleIdsByUserId(row.id())))
                .orElseThrow(() -> new BusinessException(USER_ERROR_CODE, "用户不存在"));
    }

    /**
     * 创建用户，密码使用 BCrypt 加密，并可同时分配角色。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateCommand command) {
        validateCreateCommand(command);
        if (userManageMapper.existsByUsername(command.username())) {
            throw new BusinessException(USER_ERROR_CODE, "用户名已存在");
        }

        Long userId = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();
        userManageMapper.insertUser(new UserManageMapper.UserInsertRow(
                userId,
                command.username().trim(),
                passwordService.encode(command.password()),
                trimToNull(command.nickname()),
                trimToNull(command.mobile()),
                trimToNull(command.email()),
                normalizeStatus(command.status()),
                0L,
                now,
                0L,
                now
        ));

        // 创建用户时允许同步分配角色，减少后台二次操作。
        saveRoles(userId, command.roleIds());
    }

    /**
     * 更新用户基础信息，不修改用户名和密码。
     */
    @Override
    public void updateUser(UserUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(USER_ERROR_CODE, "用户ID不能为空");
        }
        protectAdminEdit(command.id());
        ensureUserExists(command.id());

        int updated = userManageMapper.updateUser(new UserManageMapper.UserUpdateRow(
                command.id(),
                trimToNull(command.nickname()),
                trimToNull(command.mobile()),
                trimToNull(command.email()),
                normalizeStatus(command.status()),
                0L,
                LocalDateTime.now()
        ));
        if (updated == 0) {
            throw new BusinessException(USER_ERROR_CODE, "用户不存在");
        }
    }

    /**
     * 更新用户状态，默认管理员禁止调整状态。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        validateId(id);
        protectAdminStatus(id);
        int updated = userManageMapper.updateStatus(id, normalizeStatus(status));
        if (updated == 0) {
            throw new BusinessException(USER_ERROR_CODE, "用户不存在");
        }
    }

    /**
     * 逻辑删除用户，默认管理员不允许删除。
     */
    @Override
    public void deleteUser(Long id) {
        validateId(id);
        if (ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(USER_ERROR_CODE, "默认管理员不能删除");
        }
        int deleted = userManageMapper.logicalDelete(id);
        if (deleted == 0) {
            throw new BusinessException(USER_ERROR_CODE, "用户不存在");
        }
    }

    /**
     * 覆盖保存用户角色，并刷新该用户权限缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(UserRoleAssignCommand command) {
        if (command == null || command.userId() == null) {
            throw new BusinessException(USER_ERROR_CODE, "用户ID不能为空");
        }
        protectAdminRoles(command.userId());
        ensureUserExists(command.userId());
        saveRoles(command.userId(), command.roleIds());
        userPermissionService.refreshUserPermissionCache(command.userId());
    }

    /**
     * 覆盖保存用户角色关系，保存前校验角色是否存在且启用。
     */
    private void saveRoles(Long userId, List<Long> roleIds) {
        List<Long> distinctRoleIds = distinctIds(roleIds);
        userManageMapper.deleteUserRoles(userId);
        if (distinctRoleIds.isEmpty()) {
            return;
        }

        List<Long> existingRoleIds = userManageMapper.findEnabledRoleIds(distinctRoleIds);
        if (!Set.copyOf(existingRoleIds).containsAll(distinctRoleIds)) {
            throw new BusinessException(USER_ERROR_CODE, "角色不存在或已禁用");
        }
        userManageMapper.insertUserRoles(userId, distinctRoleIds);
    }

    /**
     * 校验创建用户必填字段。
     */
    private void validateCreateCommand(UserCreateCommand command) {
        if (command == null || isBlank(command.username())) {
            throw new BusinessException(USER_ERROR_CODE, "用户名不能为空");
        }
        if (isBlank(command.password())) {
            throw new BusinessException(USER_ERROR_CODE, "密码不能为空");
        }
    }

    /**
     * 确认用户存在，避免角色分配写入无效关系。
     */
    private void ensureUserExists(Long userId) {
        if (!userManageMapper.existsById(userId)) {
            throw new BusinessException(USER_ERROR_CODE, "用户不存在");
        }
    }

    /**
     * 保护默认管理员，避免基础资料被误改。
     */
    private void protectAdminEdit(Long id) {
        if (ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(USER_ERROR_CODE, "默认管理员不能编辑");
        }
    }

    /**
     * 保护默认管理员，避免状态被误调导致系统无法维护。
     */
    private void protectAdminStatus(Long id) {
        if (ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(USER_ERROR_CODE, "默认管理员不能调整状态");
        }
    }

    /**
     * 保护默认管理员，避免管理员角色被误删导致权限丢失。
     */
    private void protectAdminRoles(Long id) {
        if (ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(USER_ERROR_CODE, "默认管理员不能分配角色");
        }
    }

    /**
     * 校验 ID 不能为空。
     */
    private void validateId(Long id) {
        if (id == null) {
            throw new BusinessException(USER_ERROR_CODE, "用户ID不能为空");
        }
    }

    /**
     * 规范化状态值，只有 0 和 1 合法，空值默认启用。
     */
    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(USER_ERROR_CODE, "用户状态不合法");
        }
        return status;
    }

    /**
     * 去重并过滤空角色 ID，保留请求中的相对顺序。
     */
    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(item -> item != null && item > 0)
                .collect(Collectors.toCollection(LinkedHashSet<Long>::new))
                .stream()
                .toList();
    }

    /**
     * 将数据库查询行转换成对外响应，补充用户角色 ID。
     */
    private UserResponse toResponse(UserManageMapper.UserRow row, List<Long> roleIds) {
        return new UserResponse(
                row.id(),
                row.username(),
                row.nickname(),
                row.mobile(),
                row.email(),
                row.status(),
                row.lastLoginTime(),
                row.createdTime(),
                row.updatedTime(),
                roleIds
        );
    }

    /**
     * 将空白字符串统一转换为 null，减少数据库中的无意义空串。
     */
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 判断字符串是否为空白。
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
