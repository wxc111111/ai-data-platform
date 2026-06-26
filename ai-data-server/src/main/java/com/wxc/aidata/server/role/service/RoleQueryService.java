package com.wxc.aidata.server.role.service;

import com.wxc.aidata.server.role.response.RoleOptionResponse;

import java.util.List;

/**
 * 角色查询服务，提供轻量角色选项能力。
 */
public interface RoleQueryService {

    /**
     * 查询启用角色选项。
     */
    List<RoleOptionResponse> enabledRoleOptions();
}
