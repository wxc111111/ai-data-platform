package com.wxc.aidata.server.user.mapper;

import com.wxc.aidata.server.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 系统用户数据库访问接口，负责登录流程中的用户查询。
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据登录账号查询用户，用于登录密码校验。
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 根据用户 ID 查询用户，用于 token 恢复当前登录用户。
     */
    Optional<SysUser> findById(Long id);
}
