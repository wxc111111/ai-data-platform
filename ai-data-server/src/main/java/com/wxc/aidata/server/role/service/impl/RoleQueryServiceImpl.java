package com.wxc.aidata.server.role.service.impl;

import com.wxc.aidata.server.role.mapper.RoleQueryMapper;
import com.wxc.aidata.server.role.response.RoleOptionResponse;
import com.wxc.aidata.server.role.service.RoleQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色查询服务实现，当前只提供用户管理分配角色所需的数据。
 */
@Service
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleQueryMapper roleQueryMapper;

    /**
     * 注入角色查询 Mapper。
     */
    public RoleQueryServiceImpl(RoleQueryMapper roleQueryMapper) {
        this.roleQueryMapper = roleQueryMapper;
    }

    /**
     * 查询启用角色选项。
     */
    @Override
    public List<RoleOptionResponse> enabledRoleOptions() {
        return roleQueryMapper.findEnabledRoleOptions();
    }
}
