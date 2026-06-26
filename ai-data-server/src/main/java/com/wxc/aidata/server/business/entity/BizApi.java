package com.wxc.aidata.server.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 业务接口实体，对应 biz_api 表，保存第三方 HTTP 接口基础配置。
 */
@TableName("biz_api")
public class BizApi {

    @TableId
    private Long id;
    private Long systemId;
    private String apiCode;
    private String apiName;
    private String requestPath;
    private String requestMethod;
    private String contentType;
    private Integer connectTimeout;
    private Integer readTimeout;
    private String responseDataPath;
    private Integer status;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * MyBatis-Plus 实体需要无参构造方法完成结果映射。
     */
    public BizApi() {
    }

    /**
     * 服务层和测试组装业务接口实体时使用的完整构造方法。
     */
    public BizApi(
            Long id,
            Long systemId,
            String apiCode,
            String apiName,
            String requestPath,
            String requestMethod,
            String contentType,
            Integer connectTimeout,
            Integer readTimeout,
            String responseDataPath,
            Integer status,
            String description,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {

        this.id = id;
        this.systemId = systemId;
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.contentType = contentType;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.responseDataPath = responseDataPath;
        this.status = status;
        this.description = description;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public String getResponseDataPath() {
        return responseDataPath;
    }

    public void setResponseDataPath(String responseDataPath) {
        this.responseDataPath = responseDataPath;
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

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
