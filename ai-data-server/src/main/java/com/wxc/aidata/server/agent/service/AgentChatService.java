package com.wxc.aidata.server.agent.service;

import com.wxc.aidata.server.agent.model.AgentChatCommand;
import com.wxc.aidata.server.agent.response.AgentChatMessageResponse;
import com.wxc.aidata.server.agent.response.AgentChatResponse;
import com.wxc.aidata.server.agent.response.AgentChatSessionResponse;

import java.util.List;

/**
 * AI 问答应用服务，管理会话历史、消息落库和 Agent 调用。
 */
public interface AgentChatService {

    /**
     * 查询当前用户的历史会话列表。
     */
    List<AgentChatSessionResponse> listSessions();

    /**
     * 查询指定会话的历史消息。
     */
    List<AgentChatMessageResponse> listMessages(Long sessionId);

    /**
     * 发送一条用户消息，必要时创建新会话，并返回助手完整回答。
     */
    AgentChatResponse chat(AgentChatCommand command);

    /**
     * 发送一条用户消息并流式输出助手回答，完成后保存最终助手消息。
     */
    void streamChat(AgentChatCommand command, AgentChatStreamSink sink);
}
