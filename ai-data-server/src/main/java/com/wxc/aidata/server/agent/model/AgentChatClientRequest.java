package com.wxc.aidata.server.agent.model;

import com.wxc.aidata.server.skill.response.SkillResponse;

import java.util.List;

/**
 * AgentScope 调用请求，包含用户、会话、输入内容和当前可用 Skill 清单。
 */
public record AgentChatClientRequest(
        Long userId,
        Long sessionId,
        String message,
        List<SkillResponse> skills
) {
}
