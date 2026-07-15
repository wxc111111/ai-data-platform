package com.wxc.aidata.server.agent.model;

/**
 * AI 问答发送命令，sessionId 为空表示新建一轮对话。
 */
public record AgentChatCommand(
        Long sessionId,
        String message
) {
}
