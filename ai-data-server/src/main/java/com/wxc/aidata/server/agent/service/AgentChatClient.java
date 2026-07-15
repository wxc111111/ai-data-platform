package com.wxc.aidata.server.agent.service;

import com.wxc.aidata.server.agent.model.AgentChatClientRequest;
import com.wxc.aidata.server.agent.model.AgentChatClientResponse;

/**
 * AI Agent 客户端接口，隔离 AgentScope 调用细节，便于服务层测试。
 */
public interface AgentChatClient {

    /**
     * 调用 AgentScope 生成完整回答，并返回本轮实际调用的 Skill 摘要。
     */
    AgentChatClientResponse chat(AgentChatClientRequest request);

    /**
     * 调用 AgentScope 流式生成回答，边生成边回调增量文本。
     */
    void stream(AgentChatClientRequest request, AgentChatStreamHandler handler);
}
