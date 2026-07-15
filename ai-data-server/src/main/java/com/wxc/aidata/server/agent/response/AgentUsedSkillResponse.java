package com.wxc.aidata.server.agent.response;

import java.util.Map;

/**
 * AI 问答中被模型调用的 Skill 摘要，用于前端展示和消息记录。
 */
public record AgentUsedSkillResponse(
        Long skillId,
        String skillCode,
        String skillName,
        Map<String, Object> input,
        String output
) {
}
