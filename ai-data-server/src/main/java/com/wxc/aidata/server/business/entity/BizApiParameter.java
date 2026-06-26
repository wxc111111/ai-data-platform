package com.wxc.aidata.server.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 业务接口参数实体，对应 biz_api_parameter 表，保存第三方接口入参定义。
 */
@TableName("biz_api_parameter")
public class BizApiParameter {

    @TableId
    private Long id;
    private Long apiId;
    private String parameterName;
    private String parameterLocation;
    private String parameterType;
    private Integer required;
    private String defaultValue;
    private String description;
    private Integer sortNo;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * MyBatis-Plus 实体需要无参构造方法完成结果映射。
     */
    public BizApiParameter() {
    }

    /**
     * 服务层和测试组装参数实体时使用的完整构造方法。
     */
    public BizApiParameter(
            Long id,
            Long apiId,
            String parameterName,
            String parameterLocation,
            String parameterType,
            Integer required,
            String defaultValue,
            String description,
            Integer sortNo,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {

        this.id = id;
        this.apiId = apiId;
        this.parameterName = parameterName;
        this.parameterLocation = parameterLocation;
        this.parameterType = parameterType;
        this.required = required;
        this.defaultValue = defaultValue;
        this.description = description;
        this.sortNo = sortNo;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterLocation() {
        return parameterLocation;
    }

    public void setParameterLocation(String parameterLocation) {
        this.parameterLocation = parameterLocation;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
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
