package com.wxc.aidata.server.permission.service.impl;

import com.wxc.aidata.server.auth.service.AuthSessionManager;
import com.wxc.aidata.server.permission.mapper.PermissionManageMapper;
import com.wxc.aidata.server.permission.response.PermissionTreeResponse;
import com.wxc.aidata.server.permission.service.CurrentUserMenuService;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 当前用户菜单服务实现，按权限编码裁剪后端配置的菜单树。
 */
@Service
public class CurrentUserMenuServiceImpl implements CurrentUserMenuService {

    private static final String MENU_TYPE = "MENU";
    private static final int ENABLED_STATUS = 1;

    private final PermissionManageMapper permissionManageMapper;
    private final UserPermissionService userPermissionService;
    private final AuthSessionManager authSessionManager;

    /**
     * 注入权限、用户授权和登录会话组件。
     */
    public CurrentUserMenuServiceImpl(
            PermissionManageMapper permissionManageMapper,
            UserPermissionService userPermissionService,
            AuthSessionManager authSessionManager) {

        this.permissionManageMapper = permissionManageMapper;
        this.userPermissionService = userPermissionService;
        this.authSessionManager = authSessionManager;
    }

    /**
     * 查询当前用户权限编码，并用后端权限表生成可见菜单树。
     */
    @Override
    public List<PermissionTreeResponse> currentMenus() {
        Long userId = authSessionManager.currentUserId();
        Set<String> permissionCodes = Set.copyOf(userPermissionService.findPermissionCodes(userId));
        List<PermissionManageMapper.PermissionRow> menuRows = permissionManageMapper.findPermissions().stream()
                .filter(this::isEnabledMenu)
                .sorted(Comparator.comparing(PermissionManageMapper.PermissionRow::sortNo)
                        .thenComparing(PermissionManageMapper.PermissionRow::id))
                .toList();

        return buildVisibleTree(menuRows, permissionCodes);
    }

    /**
     * 只保留启用的菜单节点，按钮和接口权限不参与前端菜单展示。
     */
    private boolean isEnabledMenu(PermissionManageMapper.PermissionRow row) {
        return row != null
                && MENU_TYPE.equalsIgnoreCase(row.permissionType())
                && Integer.valueOf(ENABLED_STATUS).equals(row.status());
    }

    /**
     * 先按后端父子关系组树，再递归裁剪无权限分支。
     */
    private List<PermissionTreeResponse> buildVisibleTree(
            List<PermissionManageMapper.PermissionRow> rows,
            Set<String> permissionCodes) {

        Map<Long, MutableMenuNode> nodeMap = new LinkedHashMap<>();
        for (PermissionManageMapper.PermissionRow row : rows) {
            nodeMap.put(row.id(), new MutableMenuNode(row));
        }

        List<MutableMenuNode> roots = new ArrayList<>();
        for (MutableMenuNode node : nodeMap.values()) {
            Long parentId = node.row.parentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).children.add(node);
            }
        }

        return roots.stream()
                .map(node -> toVisibleResponse(node, permissionCodes))
                .filter(item -> item != null)
                .toList();
    }

    /**
     * 有权限的叶子菜单直接展示；父级菜单只要存在可见子菜单就展示。
     */
    private PermissionTreeResponse toVisibleResponse(MutableMenuNode node, Set<String> permissionCodes) {
        List<PermissionTreeResponse> visibleChildren = node.children.stream()
                .map(child -> toVisibleResponse(child, permissionCodes))
                .filter(item -> item != null)
                .toList();

        if (!shouldShowMenu(node.row, permissionCodes, visibleChildren)) {
            return null;
        }

        return new PermissionTreeResponse(
                node.row.id(),
                node.row.parentId(),
                node.row.permissionName(),
                node.row.permissionCode(),
                node.row.permissionType(),
                node.row.routePath(),
                node.row.componentPath(),
                node.row.icon(),
                node.row.sortNo(),
                node.row.status(),
                node.row.createdTime(),
                node.row.updatedTime(),
                visibleChildren
        );
    }

    /**
     * 有权限的菜单可展示；公共路由菜单可展示；公共分组必须存在可见子菜单才展示。
     */
    private boolean shouldShowMenu(
            PermissionManageMapper.PermissionRow row,
            Set<String> permissionCodes,
            List<PermissionTreeResponse> visibleChildren) {

        String permissionCode = row.permissionCode();
        if (permissionCode != null && !permissionCode.isBlank()) {
            return permissionCodes.contains(permissionCode) || !visibleChildren.isEmpty();
        }

        return hasRoutePath(row) || !visibleChildren.isEmpty();
    }

    /**
     * 判断菜单是否是可直接点击的路由菜单。
     */
    private boolean hasRoutePath(PermissionManageMapper.PermissionRow row) {
        return row.routePath() != null && !row.routePath().isBlank();
    }

    /**
     * 菜单树组装过程中的可变节点。
     */
    private static final class MutableMenuNode {

        private final PermissionManageMapper.PermissionRow row;
        private final List<MutableMenuNode> children = new ArrayList<>();

        private MutableMenuNode(PermissionManageMapper.PermissionRow row) {
            this.row = row;
        }
    }
}
