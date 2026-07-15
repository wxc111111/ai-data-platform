package com.wxc.aidata.server.agent.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 问答消息响应，包含角色、正文和本条助手消息使用的 Skill。
 */
public record AgentChatMessageResponse(
        Long id,
        Long sessionId,
        String messageRole,
        String content,
        List<AgentUsedSkillResponse> usedSkills,
        LocalDateTime createdTime
) {
}
