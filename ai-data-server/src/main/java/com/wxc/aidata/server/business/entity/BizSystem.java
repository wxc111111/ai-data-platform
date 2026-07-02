package com.wxc.aidata.server.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 业务系统实体，对应 biz_system 表，保存外部系统基础地址和认证配置。
 */
@TableName("biz_system")
public class BizSystem {

    @TableId
    private Long id;
    private String systemCode;
    private String systemName;
    private String baseUrl;
    private String authType;
    private String authConfig;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer status;
    private String description;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;

    /**
     * MyBatis-Plus 实体需要无参构造方法完成结果映射。
     */
    public BizSystem() {
    }

    /**
     * 测试和服务层组装实体时使用的完整构造方法。
     */
    public BizSystem(
            Long id,
            String systemCode,
            String systemName,
            String baseUrl,
            String authType,
            String authConfig,
            Integer connectTimeout,
            Integer readTimeout,
            Integer status,
            String description,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {

        this(id, systemCode, systemName, baseUrl, authType, authConfig, connectTimeout, readTimeout, status,
                description, null, createdTime, null, updatedTime);
    }

    /**
     * 服务层写入完整审计字段时使用的构造方法。
     */
    public BizSystem(
            Long id,
            String systemCode,
            String systemName,
            String baseUrl,
            String authType,
            String authConfig,
            Integer connectTimeout,
            Integer readTimeout,
            Integer status,
            String description,
            Long createdBy,
            LocalDateTime createdTime,
            Long updatedBy,
            LocalDateTime updatedTime) {

        this.id = id;
        this.systemCode = systemCode;
        this.systemName = systemName;
        this.baseUrl = baseUrl;
        this.authType = authType;
        this.authConfig = authConfig;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.status = status;
        this.description = description;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
