package com.wxc.aidata.server.agent.response;

import java.time.LocalDateTime;

/**
 * AI 问答会话列表响应，驱动页面左侧历史记录。
 */
public record AgentChatSessionResponse(
        Long id,
        String title,
        LocalDateTime lastMessageTime,
        LocalDateTime createdTime
) {
}
