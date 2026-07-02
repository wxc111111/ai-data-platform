package com.wxc.aidata.server.skill.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Skill 配置实体，对应 ai_skill 表，保存可发布给 AI 或业务系统调用的能力定义。
 */
@TableName("ai_skill")
public class AiSkill {

    @TableId
    private Long id;
    private String skillCode;
    private String skillName;
    private String description;
    private Long apiId;
    private String permissionCode;
    private String visibility;
    private Integer timeoutMs;
    private Integer maxResultCount;
    private Integer status;
    private Integer versionNo;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;

    /**
     * MyBatis-Plus 结果映射需要无参构造方法。
     */
    public AiSkill() {
    }

    /**
     * 服务层和测试构造完整 Skill 配置时使用。
     */
    public AiSkill(Long id, String skillCode, String skillName, String description, Long apiId, String permissionCode,
                   Integer timeoutMs, Integer maxResultCount, Integer status, Integer versionNo,
                   LocalDateTime createdTime, LocalDateTime updatedTime) {
        this(id, skillCode, skillName, description, apiId, permissionCode, "PRIVATE", timeoutMs, maxResultCount, status,
                versionNo, null, createdTime, null, updatedTime);
    }

    /**
     * 服务层写入完整审计字段时使用。
     */
    public AiSkill(Long id, String skillCode, String skillName, String description, Long apiId, String permissionCode,
                   String visibility, Integer timeoutMs, Integer maxResultCount, Integer status, Integer versionNo,
                   Long createdBy, LocalDateTime createdTime, Long updatedBy, LocalDateTime updatedTime) {
        this.id = id;
        this.skillCode = skillCode;
        this.skillName = skillName;
        this.description = description;
        this.apiId = apiId;
        this.permissionCode = permissionCode;
        this.visibility = visibility;
        this.timeoutMs = timeoutMs;
        this.maxResultCount = maxResultCount;
        this.status = status;
        this.versionNo = versionNo;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
    }

    /**
     * 兼容未传 Skill 类型的旧调用，默认按私有 Skill 保存。
     */
    public AiSkill(Long id, String skillCode, String skillName, String description, Long apiId, String permissionCode,
                   Integer timeoutMs, Integer maxResultCount, Integer status, Integer versionNo,
                   Long createdBy, LocalDateTime createdTime, Long updatedBy, LocalDateTime updatedTime) {
        this(id, skillCode, skillName, description, apiId, permissionCode, "PRIVATE", timeoutMs, maxResultCount,
                status, versionNo, createdBy, createdTime, updatedBy, updatedTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillCode() {
        return skillCode;
    }

    public void setSkillCode(String skillCode) {
        this.skillCode = skillCode;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getMaxResultCount() {
        return maxResultCount;
    }

    public void setMaxResultCount(Integer maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
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
