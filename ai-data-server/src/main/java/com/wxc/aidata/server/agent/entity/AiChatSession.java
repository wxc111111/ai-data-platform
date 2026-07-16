package com.wxc.aidata.server.agent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * AI 问答会话实体，记录用户左侧历史列表的会话摘要和审计字段。
 */
@TableName("ai_chat_session")
public class AiChatSession {

    @TableId
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdTime;
    private Long createdBy;
    private LocalDateTime updatedTime;
    private Long updatedBy;

    public AiChatSession() {
    }

    public AiChatSession(Long id, Long userId, String title, LocalDateTime lastMessageTime, LocalDateTime createdTime,
                         Long createdBy, LocalDateTime updatedTime, Long updatedBy) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.lastMessageTime = lastMessageTime;
        this.createdTime = createdTime;
        this.createdBy = createdBy;
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
