package com.wxc.aidata.server.agent.response;

import java.util.List;

/**
 * AI 问答流式事件，前端按 type 区分会话创建、文本增量、完成和错误。
 */
public record AgentChatStreamEvent(
        String type,
        Long sessionId,
        String content,
        List<AgentUsedSkillResponse> usedSkills
) {

    /**
     * 通知前端本次问答所属会话，新增会话时用于回填会话 ID。
     */
    public static AgentChatStreamEvent session(Long sessionId) {
        return new AgentChatStreamEvent("session", sessionId, null, List.of());
    }

    /**
     * 推送模型生成的增量文本。
     */
    public static AgentChatStreamEvent delta(Long sessionId, String content) {
        return new AgentChatStreamEvent("delta", sessionId, content, List.of());
    }

    /**
     * 通知前端本次问答完成，并携带 Skill 调用摘要。
     */
    public static AgentChatStreamEvent done(Long sessionId, List<AgentUsedSkillResponse> usedSkills) {
        return new AgentChatStreamEvent("done", sessionId, null, usedSkills == null ? List.of() : usedSkills);
    }

    /**
     * 通知前端模型或持久化异常。
     */
    public static AgentChatStreamEvent error(Long sessionId, String message) {
        return new AgentChatStreamEvent("error", sessionId, message, List.of());
    }
}
