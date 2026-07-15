package com.wxc.aidata.server.agent.service;

import com.wxc.aidata.server.agent.response.AgentChatStreamEvent;

/**
 * AI 问答流式输出接收器，控制器用它把服务层事件转成 SSE。
 */
@FunctionalInterface
public interface AgentChatStreamSink {

    /**
     * 发送一个流式事件给前端。
     */
    void send(AgentChatStreamEvent event);
}
