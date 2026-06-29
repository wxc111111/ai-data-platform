package com.wxc.aidata.server.skill.model;

import java.util.Map;

/**
 * Skill 在线测试命令，保存调用方按 Skill 入参传入的测试值。
 */
public record SkillTestCommand(Map<String, Object> parameterValues) {
}
