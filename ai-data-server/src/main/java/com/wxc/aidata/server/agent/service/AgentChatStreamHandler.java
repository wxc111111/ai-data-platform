package com.wxc.aidata.server.agent.service;

import com.wxc.aidata.server.agent.response.AgentUsedSkillResponse;

import java.util.List;

/**
 * Agent 模型流式回调，负责把模型增量文本和最终结果交还给应用服务。
 */
public interface AgentChatStreamHandler {

    /**
     * 推送模型本次生成的增量文本。
     */
    void onText(String delta);

    /**
     * 模型输出完成后返回完整答案和本轮 Skill 调用摘要。
     */
    void onComplete(String answer, List<AgentUsedSkillResponse> usedSkills);
}
