package com.wxc.aidata.server.role.mapper;

import com.wxc.aidata.server.role.response.RoleOptionResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色查询数据库访问接口，提供用户管理所需的角色选项。
 */
@Mapper
public interface RoleQueryMapper {

    /**
     * 查询全部启用角色选项。
     */
    List<RoleOptionResponse> findEnabledRoleOptions();
}
