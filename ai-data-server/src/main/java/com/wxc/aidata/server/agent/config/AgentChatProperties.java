package com.wxc.aidata.server.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 问答配置，控制 AgentScope 模型、状态存储目录和系统提示词。
 */
@ConfigurationProperties(prefix = "ai-data.agent")
public record AgentChatProperties(
        String model,
        String statePath,
        String systemPrompt
) {

    public AgentChatProperties {
        model = model == null || model.isBlank() ? "dashscope:qwen-plus" : model;
        statePath = statePath == null || statePath.isBlank() ? ".agentscope/state/ai-data-agent" : statePath;
        systemPrompt = systemPrompt == null || systemPrompt.isBlank()
                ? "你是 AI 数据服务中台的问答助手。需要查询业务数据时，只能调用已授权的 Skill；回答要简洁、准确。"
                : systemPrompt;
    }
}
