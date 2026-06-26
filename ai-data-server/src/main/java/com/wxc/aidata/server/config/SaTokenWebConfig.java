package com.wxc.aidata.server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.wxc.aidata.server.audit.web.OperationLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token Web 配置，启用接口登录态、注解权限校验和操作审计拦截器。
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

    private final OperationLogInterceptor operationLogInterceptor;

    /**
     * 注入操作日志拦截器，确保接口鉴权前后都能进入审计链路。
     */
    public SaTokenWebConfig(OperationLogInterceptor operationLogInterceptor) {
        this.operationLogInterceptor = operationLogInterceptor;
    }

    /**
     * 注册操作日志和 Sa-Token 拦截器，登录接口放行，其余 API 先进入审计链路再校验登录态。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");

        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()).isAnnotation(true))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
