package com.wxc.aidata.server.skill.request;

import java.util.Map;

/**
 * Skill 在线测试请求体。
 */
public record SkillTestRequest(Map<String, Object> parameterValues) {
}
