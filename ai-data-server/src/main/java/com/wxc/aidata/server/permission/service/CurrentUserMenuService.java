package com.wxc.aidata.server.permission.service;

import com.wxc.aidata.server.permission.response.PermissionTreeResponse;

import java.util.List;

/**
 * 当前用户菜单服务，负责根据登录用户权限返回可见菜单树。
 */
public interface CurrentUserMenuService {

    /**
     * 查询当前登录用户可见的后端菜单树。
     */
    List<PermissionTreeResponse> currentMenus();
}
