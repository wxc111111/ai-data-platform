package com.wxc.aidata.server.skill.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Skill 参数实体，对应 ai_skill_parameter 表，维护 Skill 入参到业务接口参数的映射关系。
 */
@TableName("ai_skill_parameter")
public class AiSkillParameter {

    @TableId
    private Long id;
    private Long skillId;
    private String parameterName;
    private String parameterType;
    private Integer required;
    private String description;
    private String apiParameterName;
    private String defaultValue;
    private String valueSource;
    private String validationRule;
    private Integer sortNo;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * MyBatis-Plus 结果映射需要无参构造方法。
     */
    public AiSkillParameter() {
    }

    /**
     * 服务层保存参数映射时使用完整构造方法。
     */
    public AiSkillParameter(Long id, Long skillId, String parameterName, String parameterType, Integer required,
                            String description, String apiParameterName, String defaultValue, String valueSource,
                            String validationRule, Integer sortNo, LocalDateTime createdTime, LocalDateTime updatedTime) {
        this.id = id;
        this.skillId = skillId;
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.required = required;
        this.description = description;
        this.apiParameterName = apiParameterName;
        this.defaultValue = defaultValue;
        this.valueSource = valueSource;
        this.validationRule = validationRule;
        this.sortNo = sortNo;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public String getParameterType() { return parameterType; }
    public void setParameterType(String parameterType) { this.parameterType = parameterType; }
    public Integer getRequired() { return required; }
    public void setRequired(Integer required) { this.required = required; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getApiParameterName() { return apiParameterName; }
    public void setApiParameterName(String apiParameterName) { this.apiParameterName = apiParameterName; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getValueSource() { return valueSource; }
    public void setValueSource(String valueSource) { this.valueSource = valueSource; }
    public String getValidationRule() { return validationRule; }
    public void setValidationRule(String validationRule) { this.validationRule = validationRule; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
