package com.wxc.aidata.server.auth.service;

import com.wxc.aidata.server.auth.model.LoginCommand;
import com.wxc.aidata.server.auth.model.LoginSession;
import com.wxc.aidata.server.auth.model.LoginUser;

import java.util.List;

/**
 * 登录认证业务接口，统一定义登录、退出、当前用户和权限查询能力。
 */
public interface AuthService {

    /**
     * 校验登录账号和密码，成功后创建登录会话并返回 token 信息。
     */
    LoginSession login(LoginCommand command);

    /**
     * 退出当前登录会话，清理服务端 token 状态。
     */
    void logout();

    /**
     * 获取当前 token 对应的登录用户信息。
     */
    LoginUser currentUser();

    /**
     * 获取当前 token 对应用户的权限编码列表。
     */
    List<String> currentPermissions();
}
