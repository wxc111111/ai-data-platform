package com.wxc.aidata.server.agent.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.agent.model.AgentChatCommand;
import com.wxc.aidata.server.agent.request.AgentChatRequest;
import com.wxc.aidata.server.agent.response.AgentChatMessageResponse;
import com.wxc.aidata.server.agent.response.AgentChatResponse;
import com.wxc.aidata.server.agent.response.AgentChatSessionResponse;
import com.wxc.aidata.server.agent.response.AgentChatStreamEvent;
import com.wxc.aidata.server.agent.service.AgentChatService;
import com.wxc.aidata.server.agent.service.AgentChatStreamSink;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AI 问答控制器，提供历史会话、消息历史、普通发送和流式发送接口。
 */
@RestController
@RequestMapping("api/agent-chat")
public class AgentChatController {

    private final AgentChatService agentChatService;
    private final ObjectMapper objectMapper;

    /**
     * 注入 AI 问答应用服务和 JSON 序列化工具。
     */
    public AgentChatController(AgentChatService agentChatService, ObjectMapper objectMapper) {
        this.agentChatService = agentChatService;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询当前用户可见的历史会话列表。
     */
    @GetMapping("/sessions")
    @SaCheckPermission("system:agent-chat:list")
    public Result<List<AgentChatSessionResponse>> listSessions() {
        return Result.success(agentChatService.listSessions());
    }

    /**
     * 查询指定会话的历史消息。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @SaCheckPermission("system:agent-chat:list")
    public Result<List<AgentChatMessageResponse>> listMessages(@PathVariable("sessionId") Long sessionId) {
        return Result.success(agentChatService.listMessages(sessionId));
    }

    /**
     * 发送用户消息并返回助手完整回答，保留给非流式调用兼容。
     */
    @PostMapping("/chat")
    @SaCheckPermission("system:agent-chat:chat")
    public Result<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        return Result.success(agentChatService.chat(new AgentChatCommand(request.sessionId(), request.message())));
    }

    /**
     * 发送用户消息并以 SSE 方式流式返回模型增量输出。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckPermission("system:agent-chat:chat")
    public void streamChat(@RequestBody AgentChatRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setHeader("Cache-Control", "no-cache");
        try {
            agentChatService.streamChat(new AgentChatCommand(request.sessionId(), request.message()), new AgentChatStreamSink() {
                @Override
                public void send(AgentChatStreamEvent event) {
                    writeEvent(response, event);
                }
            });
        } catch (Exception exception) {
            writeEvent(response, AgentChatStreamEvent.error(request.sessionId(), exception.getMessage()));
        }
    }

    /**
     * 按 SSE 协议写出 data 行并立即 flush，让前端能边生成边展示。
     */
    private void writeEvent(HttpServletResponse response, AgentChatStreamEvent event) {
        try {
            response.getWriter().write("data: ");
            response.getWriter().write(objectMapper.writeValueAsString(event));
            response.getWriter().write("\n\n");
            response.getWriter().flush();
        } catch (IOException exception) {
            throw new IllegalStateException("写出 AI 流式响应失败", exception);
        }
    }
}
