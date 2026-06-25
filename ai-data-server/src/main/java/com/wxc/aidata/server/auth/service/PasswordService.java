package com.wxc.aidata.server.auth.service;

/**
 * 密码服务接口，隔离密码加密和校验实现，便于后续更换加密策略。
 */
public interface PasswordService {

    /**
     * 生成密码密文；同一个明文密码每次都应生成不同密文，降低泄露后的撞库风险。
     */
    String encode(String rawPassword);

    /**
     * 校验用户输入的明文密码是否匹配数据库中的密文密码。
     */
    boolean matches(String rawPassword, String encodedPassword);
}
