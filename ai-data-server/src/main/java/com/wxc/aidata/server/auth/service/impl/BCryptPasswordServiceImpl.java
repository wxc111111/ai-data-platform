package com.wxc.aidata.server.auth.service.impl;

import com.wxc.aidata.server.auth.service.PasswordService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * BCrypt 密码服务实现，使用随机盐保证相同密码不会生成相同密文。
 */
@Service
public class BCryptPasswordServiceImpl implements PasswordService {

    /**
     * 生成 BCrypt 密文；强度 12 在安全性和登录性能之间做平衡。
     */
    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    /**
     * 校验明文密码和 BCrypt 密文是否匹配，空值直接视为失败。
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        // 密码为空或数据库密文异常时不抛出底层异常，统一按校验失败处理。
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
