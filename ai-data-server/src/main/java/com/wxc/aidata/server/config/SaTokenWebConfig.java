package com.wxc.aidata.server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token Web 配置，启用接口登录态和注解权限校验。
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器，登录接口放行，其余 API 先校验登录态，再执行注解权限校验。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()).isAnnotation(true))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
