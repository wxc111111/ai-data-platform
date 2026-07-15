package com.wxc.aidata.server.agent.response;

import java.util.List;

/**
 * AI 问答发送响应，返回会话 ID、助手答案和工具调用情况。
 */
public record AgentChatResponse(
        Long sessionId,
        String answer,
        List<AgentUsedSkillResponse> usedSkills
) {
}
