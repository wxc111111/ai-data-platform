package com.wxc.aidata.server.permission.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.server.common.id.IdGenerator;
import com.wxc.aidata.server.permission.mapper.PermissionManageMapper;
import com.wxc.aidata.server.permission.model.PermissionCreateCommand;
import com.wxc.aidata.server.permission.model.PermissionUpdateCommand;
import com.wxc.aidata.server.permission.response.PermissionTreeResponse;
import com.wxc.aidata.server.permission.service.PermissionManageService;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理服务实现，负责权限树维护和权限变更后的用户缓存刷新。
 */
@Service
public class PermissionManageServiceImpl implements PermissionManageService {

    private static final int PERMISSION_ERROR_CODE = 11003;

    private static final Set<String> SUPPORTED_TYPES = Set.of("MENU", "BUTTON", "API");

    private final PermissionManageMapper permissionManageMapper;
    private final IdGenerator idGenerator;
    private final UserPermissionService userPermissionService;

    /**
     * 注入权限管理 Mapper、ID 生成器和用户权限缓存服务。
     */
    public PermissionManageServiceImpl(
            PermissionManageMapper permissionManageMapper,
            IdGenerator idGenerator,
            UserPermissionService userPermissionService) {

        this.permissionManageMapper = permissionManageMapper;
        this.idGenerator = idGenerator;
        this.userPermissionService = userPermissionService;
    }

    /**
     * 查询全部权限并组装成树形结构。
     */
    @Override
    public List<PermissionTreeResponse> treePermissions() {
        List<PermissionManageMapper.PermissionRow> rows = permissionManageMapper.findPermissions();
        rows = rows.stream()
                .sorted(Comparator.comparing(PermissionManageMapper.PermissionRow::sortNo)
                        .thenComparing(PermissionManageMapper.PermissionRow::id))
                .toList();
        return buildTree(rows);
    }

    /**
     * 查询权限详情，详情不需要携带子节点。
     */
    @Override
    public PermissionTreeResponse getPermission(Long id) {
        validateId(id);
        return permissionManageMapper.findPermissionById(id)
                .map(row -> toResponse(row, List.of()))
                .orElseThrow(() -> new BusinessException(PERMISSION_ERROR_CODE, "权限不存在"));
    }

    /**
     * 创建权限，父节点必须存在且权限编码非空时必须唯一。
     */
    @Override
    public void createPermission(PermissionCreateCommand command) {
        validateCreateCommand(command);
        Long parentId = normalizeParentId(command.parentId());
        ensureParentExists(parentId);
        String permissionCode = trimToNull(command.permissionCode());
        if (permissionCode != null && permissionManageMapper.existsByPermissionCode(permissionCode)) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限编码已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        permissionManageMapper.insertPermission(new PermissionManageMapper.PermissionInsertRow(
                idGenerator.nextId(),
                parentId,
                command.permissionName().trim(),
                permissionCode,
                normalizeType(command.permissionType()),
                trimToNull(command.routePath()),
                trimToNull(command.componentPath()),
                trimToNull(command.icon()),
                normalizeSortNo(command.sortNo()),
                normalizeStatus(command.status()),
                now,
                now
        ));
    }

    /**
     * 更新权限，修改权限编码或状态后刷新已授权用户缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(PermissionUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限ID不能为空");
        }
        validateUpdateCommand(command);
        Long parentId = normalizeParentId(command.parentId());
        if (command.id().equals(parentId)) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "父权限不能选择自身");
        }
        ensureParentExists(parentId);
        String permissionCode = trimToNull(command.permissionCode());
        if (permissionCode != null && permissionManageMapper.existsByPermissionCodeExcludeId(permissionCode, command.id())) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限编码已存在");
        }

        List<Long> userIds = permissionManageMapper.findUserIdsByPermissionId(command.id());
        int updated = permissionManageMapper.updatePermission(new PermissionManageMapper.PermissionUpdateRow(
                command.id(),
                parentId,
                command.permissionName().trim(),
                permissionCode,
                normalizeType(command.permissionType()),
                trimToNull(command.routePath()),
                trimToNull(command.componentPath()),
                trimToNull(command.icon()),
                normalizeSortNo(command.sortNo()),
                normalizeStatus(command.status()),
                LocalDateTime.now()
        ));
        if (updated == 0) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限不存在");
        }
        refreshUsers(userIds);
    }

    /**
     * 启用或禁用权限，状态变化后刷新已授权用户缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        validateId(id);
        List<Long> userIds = permissionManageMapper.findUserIdsByPermissionId(id);
        int updated = permissionManageMapper.updateStatus(id, normalizeStatus(status));
        if (updated == 0) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限不存在");
        }
        refreshUsers(userIds);
    }

    /**
     * 删除权限，存在子权限或非 admin 角色授权时不允许删除。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        validateId(id);
        if (permissionManageMapper.countChildren(id) > 0) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "存在子权限，不能删除");
        }
        if (permissionManageMapper.countNonAdminRolesByPermissionId(id) > 0) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限已分配给角色，不能删除");
        }
        // admin 角色默认拥有全部启用权限，这里的显式授权关系是冗余数据，删除权限前可以清理。
        permissionManageMapper.deleteRolePermissionsByPermissionId(id);
        int deleted = permissionManageMapper.deletePermission(id);
        if (deleted == 0) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限不存在");
        }
    }

    /**
     * 组装权限树，保留查询排序后的节点顺序。
     */
    private List<PermissionTreeResponse> buildTree(List<PermissionManageMapper.PermissionRow> rows) {
        Map<Long, MutablePermissionNode> nodeMap = new LinkedHashMap<>();
        for (PermissionManageMapper.PermissionRow row : rows) {
            nodeMap.put(row.id(), new MutablePermissionNode(row));
        }

        List<MutablePermissionNode> roots = new ArrayList<>();
        for (MutablePermissionNode node : nodeMap.values()) {
            if (node.row.parentId() == null || node.row.parentId() == 0 || !nodeMap.containsKey(node.row.parentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.row.parentId()).children.add(node);
            }
        }
        return roots.stream().map(this::toTreeResponse).toList();
    }

    /**
     * 校验创建权限必填字段。
     */
    private void validateCreateCommand(PermissionCreateCommand command) {
        if (command == null || isBlank(command.permissionName())) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限名称不能为空");
        }
        normalizeType(command.permissionType());
    }

    /**
     * 校验更新权限必填字段。
     */
    private void validateUpdateCommand(PermissionUpdateCommand command) {
        if (isBlank(command.permissionName())) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限名称不能为空");
        }
        normalizeType(command.permissionType());
    }

    /**
     * 父节点非根节点时必须存在。
     */
    private void ensureParentExists(Long parentId) {
        if (parentId != 0 && !permissionManageMapper.existsById(parentId)) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "父权限不存在");
        }
    }

    /**
     * 刷新权限关联用户的缓存。
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
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限ID不能为空");
        }
    }

    /**
     * 权限类型只允许菜单、按钮和接口。
     */
    private String normalizeType(String type) {
        if (isBlank(type)) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限类型不能为空");
        }
        String normalized = type.trim().toUpperCase();
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限类型不合法");
        }
        return normalized;
    }

    /**
     * 规范化父节点，空值视为根节点。
     */
    private Long normalizeParentId(Long parentId) {
        return parentId == null || parentId < 0 ? 0L : parentId;
    }

    /**
     * 规范化排序号，空值默认 0。
     */
    private Integer normalizeSortNo(Integer sortNo) {
        return sortNo == null ? 0 : sortNo;
    }

    /**
     * 规范化状态，空值默认启用。
     */
    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(PERMISSION_ERROR_CODE, "权限状态不合法");
        }
        return status;
    }

    /**
     * 将空白字符串转换成 null。
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
     * 递归转换可变节点为不可变响应对象。
     */
    private PermissionTreeResponse toTreeResponse(MutablePermissionNode node) {
        return toResponse(node.row, node.children.stream().map(this::toTreeResponse).toList());
    }

    /**
     * 将数据库行转换成权限树响应。
     */
    private PermissionTreeResponse toResponse(PermissionManageMapper.PermissionRow row, List<PermissionTreeResponse> children) {
        return new PermissionTreeResponse(
                row.id(),
                row.parentId(),
                row.permissionName(),
                row.permissionCode(),
                row.permissionType(),
                row.routePath(),
                row.componentPath(),
                row.icon(),
                row.sortNo(),
                row.status(),
                row.createdTime(),
                row.updatedTime(),
                children
        );
    }

    /**
     * 权限树组装过程中的可变节点。
     */
    private static final class MutablePermissionNode {

        private final PermissionManageMapper.PermissionRow row;
        private final List<MutablePermissionNode> children = new ArrayList<>();

        private MutablePermissionNode(PermissionManageMapper.PermissionRow row) {
            this.row = row;
        }
    }
}
