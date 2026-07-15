package com.wxc.aidata.server.agent.model;

import com.wxc.aidata.server.agent.response.AgentUsedSkillResponse;

import java.util.List;

/**
 * AgentScope 调用响应，保留最终回答和本轮执行过的 Skill 摘要。
 */
public record AgentChatClientResponse(
        String answer,
        List<AgentUsedSkillResponse> usedSkills
) {
}
