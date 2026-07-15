package com.wxc.aidata.server.agent.request;

/**
 * AI 问答发送请求，前端新会话时不传 sessionId。
 */
public record AgentChatRequest(
        Long sessionId,
        String message
) {
}
