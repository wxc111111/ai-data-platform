package com.wxc.aidata.server.agent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * AI 问答消息实体，保存用户输入、助手回答以及本轮 Skill 调用摘要。
 */
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId
    private Long id;
    private Long sessionId;
    private Long userId;
    private String messageRole;
    private String content;
    private String usedSkillsJson;
    private LocalDateTime createdTime;
    private Long createdBy;

    public AiChatMessage() {
    }

    public AiChatMessage(Long id, Long sessionId, Long userId, String messageRole, String content,
                         String usedSkillsJson, LocalDateTime createdTime, Long createdBy) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.messageRole = messageRole;
        this.content = content;
        this.usedSkillsJson = usedSkillsJson;
        this.createdTime = createdTime;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessageRole() {
        return messageRole;
    }

    public String getContent() {
        return content;
    }

    public String getUsedSkillsJson() {
        return usedSkillsJson;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
