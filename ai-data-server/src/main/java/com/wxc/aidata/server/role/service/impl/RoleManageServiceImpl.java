package com.wxc.aidata.server.role.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.common.id.IdGenerator;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import com.wxc.aidata.server.role.mapper.RoleManageMapper;
import com.wxc.aidata.server.role.model.RoleCreateCommand;
import com.wxc.aidata.server.role.model.RolePageQuery;
import com.wxc.aidata.server.role.model.RoleUpdateCommand;
import com.wxc.aidata.server.role.response.RoleResponse;
import com.wxc.aidata.server.role.service.RoleManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色管理服务实现，负责角色 CRUD、管理员角色保护和权限缓存刷新。
 */
@Service
public class RoleManageServiceImpl implements RoleManageService {

    /**
     * 默认管理员角色 ID，初始化脚本固定为 1。
     */
    private static final Long ADMIN_ROLE_ID = 1L;

    /**
     * 默认管理员角色编码，避免未来 ID 迁移时漏掉保护。
     */
    private static final String ADMIN_ROLE_CODE = "admin";

    private static final int ROLE_ERROR_CODE = 11002;

    private final RoleManageMapper roleManageMapper;
    private final IdGenerator idGenerator;
    private final UserPermissionService userPermissionService;

    /**
     * 注入角色管理 Mapper、ID 生成器和用户权限缓存服务。
     */
    public RoleManageServiceImpl(
            RoleManageMapper roleManageMapper,
            IdGenerator idGenerator,
            UserPermissionService userPermissionService) {

        this.roleManageMapper = roleManageMapper;
        this.idGenerator = idGenerator;
        this.userPermissionService = userPermissionService;
    }

    /**
     * 分页查询角色列表，使用 PageHelper 保持与用户管理一致的分页方式。
     */
    @Override
    public PageResult<RoleResponse> pageRoles(RolePageQuery query) {
        RolePageQuery safeQuery = query == null ? new RolePageQuery(1, 10, null, null) : query;
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<RoleManageMapper.RoleRow> rows = roleManageMapper.findRoles(safeQuery);
        System.out.println("Role rows: " + rows);
        PageInfo<RoleManageMapper.RoleRow> pageInfo = new PageInfo<>(rows);
        List<RoleResponse> records = rows.stream().map(this::toResponse).toList();
        System.out.println("Role responses: " + records);
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 查询角色详情，角色不存在时返回业务异常。
     */
    @Override
    public RoleResponse getRole(Long id) {
        validateId(id);
        return roleManageMapper.findRoleById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ROLE_ERROR_CODE, "角色不存在"));
    }

    /**
     * 创建角色，角色编码必须唯一。
     */
    @Override
    public void createRole(RoleCreateCommand command) {
        validateCreateCommand(command);
        String roleCode = command.roleCode().trim();
        if (roleManageMapper.existsByRoleCode(roleCode)) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色编码已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        roleManageMapper.insertRole(new RoleManageMapper.RoleInsertRow(
                idGenerator.nextId(),
                roleCode,
                command.roleName().trim(),
                normalizeStatus(command.status()),
                trimToNull(command.description()),
                now,
                now
        ));
    }

    /**
     * 更新角色基础信息，默认管理员角色禁止修改。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色ID不能为空");
        }
        protectAdminUpdate(command.id(), command.roleCode());
        validateUpdateCommand(command);
        String roleCode = command.roleCode().trim();
        if (roleManageMapper.existsByRoleCodeExcludeId(roleCode, command.id())) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色编码已存在");
        }

        List<Long> userIds = roleManageMapper.findUserIdsByRoleId(command.id());
        int updated = roleManageMapper.updateRole(new RoleManageMapper.RoleUpdateRow(
                command.id(),
                roleCode,
                command.roleName().trim(),
                normalizeStatus(command.status()),
                trimToNull(command.description()),
                LocalDateTime.now()
        ));
        if (updated == 0) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色不存在");
        }
        refreshUsers(userIds);
    }

    /**
     * 启用或禁用角色，状态变化后刷新已分配用户的权限缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        validateId(id);
        protectAdminStatus(id);
        List<Long> userIds = roleManageMapper.findUserIdsByRoleId(id);
        int updated = roleManageMapper.updateStatus(id, normalizeStatus(status));
        if (updated == 0) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色不存在");
        }
        refreshUsers(userIds);
    }

    /**
     * 删除角色，已分配给用户的角色不允许删除。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        validateId(id);
        protectAdminDelete(id);
        if (!roleManageMapper.existsById(id)) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色不存在");
        }
        if (roleManageMapper.countUsersByRoleId(id) > 0) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色已分配给用户，不能删除");
        }
        roleManageMapper.deleteRolePermissions(id);
        int deleted = roleManageMapper.deleteRole(id);
        if (deleted == 0) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色不存在");
        }
    }

    /**
     * 校验创建角色必填字段。
     */
    private void validateCreateCommand(RoleCreateCommand command) {
        if (command == null || isBlank(command.roleCode())) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色编码不能为空");
        }
        if (isBlank(command.roleName())) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色名称不能为空");
        }
    }

    /**
     * 校验更新角色必填字段。
     */
    private void validateUpdateCommand(RoleUpdateCommand command) {
        if (isBlank(command.roleCode())) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色编码不能为空");
        }
        if (isBlank(command.roleName())) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色名称不能为空");
        }
    }

    /**
     * 默认管理员角色不能修改，避免系统最高权限被误改。
     */
    private void protectAdminUpdate(Long id, String roleCode) {
        if (ADMIN_ROLE_ID.equals(id) || ADMIN_ROLE_CODE.equals(roleCode)) {
            throw new BusinessException(ROLE_ERROR_CODE, "默认管理员角色不能修改");
        }
    }

    /**
     * 默认管理员角色不能调整状态，避免管理员权限失效。
     */
    private void protectAdminStatus(Long id) {
        if (ADMIN_ROLE_ID.equals(id)) {
            throw new BusinessException(ROLE_ERROR_CODE, "默认管理员角色不能调整状态");
        }
    }

    /**
     * 默认管理员角色不能删除。
     */
    private void protectAdminDelete(Long id) {
        if (ADMIN_ROLE_ID.equals(id)) {
            throw new BusinessException(ROLE_ERROR_CODE, "默认管理员角色不能删除");
        }
    }

    /**
     * 刷新角色关联用户的权限缓存，确保角色状态或编码变化立即生效。
     */
    private void refreshUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            userPermissionService.refreshUserPermissionCache(userId);
        }
    }

    /**
     * 校验 ID 不能为空。
     */
    private void validateId(Long id) {
        if (id == null) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色ID不能为空");
        }
    }

    /**
     * 规范化角色状态，空值默认启用。
     */
    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ROLE_ERROR_CODE, "角色状态不合法");
        }
        return status;
    }

    /**
     * 空白字符串统一转为 null，避免数据库保存无意义空串。
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

    /**
     * 将数据库行转换成角色响应对象。
     */
    private RoleResponse toResponse(RoleManageMapper.RoleRow row) {
        return new RoleResponse(
                row.id(),
                row.roleCode(),
                row.roleName(),
                row.status(),
                row.description(),
                row.createdTime(),
                row.updatedTime()
        );
    }
}
