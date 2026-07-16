package com.wxc.aidata.server.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.server.agent.config.AgentChatProperties;
import com.wxc.aidata.server.agent.model.AgentChatClientRequest;
import com.wxc.aidata.server.agent.model.AgentChatClientResponse;
import com.wxc.aidata.server.agent.response.AgentUsedSkillResponse;
import com.wxc.aidata.server.agent.service.AgentChatClient;
import com.wxc.aidata.server.agent.service.AgentChatStreamHandler;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.permission.model.ResourceAccessScope;
import com.wxc.aidata.server.skill.model.SkillTestCommand;
import com.wxc.aidata.server.skill.response.SkillParameterResponse;
import com.wxc.aidata.server.skill.response.SkillResponse;
import com.wxc.aidata.server.skill.service.SkillService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionDecision;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.core.tool.ToolBase;
import io.agentscope.core.tool.ToolCallParam;
import io.agentscope.core.tool.Toolkit;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * AgentScope Java 2.0 客户端，把当前用户可见 Skill 注册成模型可调用工具。
 */
@Component
public class AgentScopeChatClient implements AgentChatClient {

    private static final int AGENT_ERROR_CODE = 4001;
    private static final Pattern UNSAFE_TOOL_CHAR = Pattern.compile("[^A-Za-z0-9_]");

    private final SkillService skillService;
    private final AgentChatProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * 注入 Skill 执行服务、Agent 配置和 JSON 工具。
     */
    public AgentScopeChatClient(SkillService skillService, AgentChatProperties properties, ObjectMapper objectMapper) {
        this.skillService = skillService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 非流式调用保留兼容能力，内部复用流式调用并汇总最终文本。
     */
    @Override
    public AgentChatClientResponse chat(AgentChatClientRequest request) {
        List<AgentUsedSkillResponse> usedSkills = new ArrayList<>();
        StringBuilder answer = new StringBuilder();
        stream(request, new AgentChatStreamHandler() {
            @Override
            public void onText(String delta) {
                // 非流式接口只关心最终 onComplete，增量文本不单独返回。
            }

            @Override
            public void onComplete(String finalAnswer, List<AgentUsedSkillResponse> finalUsedSkills) {
                answer.append(finalAnswer == null ? "" : finalAnswer);
                usedSkills.addAll(finalUsedSkills == null ? List.of() : finalUsedSkills);
            }
        });
        return new AgentChatClientResponse(answer.toString(), usedSkills);
    }

    /**
     * 每次请求按当前授权 Skill 构造 Toolkit，并使用 userId/sessionId 恢复 AgentScope 会话状态。
     */
    @Override
    public void stream(AgentChatClientRequest request, AgentChatStreamHandler handler) {
        List<AgentUsedSkillResponse> usedSkills = new CopyOnWriteArrayList<>();
        Toolkit toolkit = new Toolkit();
        for (SkillResponse skill : request.skills()) {
            toolkit.registerTool(new SkillTool(skill, skillService, objectMapper, usedSkills, request.accessScope()));
        }

        try {
            ReActAgent agent = ReActAgent.builder()
                    .name("ai-data-agent")
                    .sysPrompt(properties.systemPrompt())
                    .model(properties.model())
                    .toolkit(toolkit)
                    .stateStore(new JsonFileAgentStateStore(Path.of(properties.statePath())))
                    .build();
            RuntimeContext context = RuntimeContext.builder()
                    .userId(String.valueOf(request.userId()))
                    .sessionId(String.valueOf(request.sessionId()))
                    .build();

            StringBuilder answer = new StringBuilder();
            String finalAnswer = null;
            for (AgentEvent event : agent.streamEvents(List.of(new UserMessage(request.message())), context).toIterable()) {
                if (event instanceof TextBlockDeltaEvent textEvent) {
                    String delta = textEvent.getDelta();
                    if (delta != null) {
                        answer.append(delta);
                        handler.onText(delta);
                    }
                }
                if (event instanceof AgentResultEvent resultEvent) {
                    Msg result = resultEvent.getResult();
                    finalAnswer = result == null ? "" : result.getTextContent();
                }
            }
            handler.onComplete(finalAnswer == null ? answer.toString() : finalAnswer, List.copyOf(usedSkills));
        } catch (Exception e) {
            throw new BusinessException(AGENT_ERROR_CODE, "AI 问答调用失败：" + e.getMessage());
        }
    }

    /**
     * Skill 工具适配器，把 Skill 参数转换为 JSON Schema，并复用 SkillService 的在线测试执行能力。
     */
    private static final class SkillTool extends ToolBase {

        private final SkillResponse skill;
        private final SkillService skillService;
        private final ObjectMapper objectMapper;
        private final List<AgentUsedSkillResponse> usedSkills;
        private final ResourceAccessScope accessScope;

        private SkillTool(SkillResponse skill, SkillService skillService, ObjectMapper objectMapper,
                          List<AgentUsedSkillResponse> usedSkills, ResourceAccessScope accessScope) {
            super(ToolBase.builder()
                    .name(toolName(skill.skillCode()))
                    .description(skill.description())
                    .inputSchema(inputSchema(skill.parameters()))
                    .readOnly(true)
                    .concurrencySafe(false));
            this.skill = skill;
            this.skillService = skillService;
            this.objectMapper = objectMapper;
            this.usedSkills = usedSkills;
            this.accessScope = accessScope;
        }

        /**
         * Skill 属于当前请求授权清单，工具执行阶段直接放行。
         */
        @Override
        public Mono<PermissionDecision> checkPermissions(Map<String, Object> toolInput, PermissionContextState context) {
            return Mono.just(PermissionDecision.allow("当前用户已通过 Skill 权限校验。"));
        }

        /**
         * 执行 Skill 并把业务接口返回值转换为模型可阅读文本。
         */
        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            return Mono.fromCallable(new Callable<>() {
                @Override
                public ToolResultBlock call() throws Exception {
                    Map<String, Object> input = param.getInput() == null ? Map.of() : param.getInput();
                    // 工具回调可能运行在非 Web 线程，必须使用请求进入 AgentScope 前固化的权限快照。
                    BusinessApiTestResponse response = skillService.testSkill(
                            skill.id(), new SkillTestCommand(input), accessScope
                    );
                    String output = outputText(response);
                    usedSkills.add(new AgentUsedSkillResponse(skill.id(), skill.skillCode(), skill.skillName(), input, output));
                    return ToolResultBlock.builder()
                            .id(param.getToolUseBlock().getId())
                            .name(getName())
                            .output(List.of(TextBlock.builder().text(output).build()))
                            .build();
                }
            });
        }

        /**
         * 优先把提取后的业务数据返回给模型，未配置提取时回退到完整响应体。
         */
        private String outputText(BusinessApiTestResponse response) throws JsonProcessingException {
            Object data = response.extractedData() == null ? response.body() : response.extractedData();
            return data instanceof String text ? text : objectMapper.writeValueAsString(data);
        }

        /**
         * 将 Skill 编码规范成 AgentScope 工具名，避免短横线等字符被模型工具 schema 拒绝。
         */
        private static String toolName(String skillCode) {
            String normalized = UNSAFE_TOOL_CHAR.matcher(skillCode == null ? "skill" : skillCode).replaceAll("_");
            return normalized.startsWith("skill_") ? normalized : "skill_" + normalized;
        }

        /**
         * 仅把 CALLER 来源参数暴露给模型，CONSTANT 参数由 SkillService 执行时自动补全。
         */
        private static Map<String, Object> inputSchema(List<SkillParameterResponse> parameters) {
            Map<String, Object> properties = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();
            for (SkillParameterResponse parameter : parameters == null ? List.<SkillParameterResponse>of() : parameters) {
                if ("CONSTANT".equalsIgnoreCase(parameter.valueSource())) {
                    continue;
                }
                properties.put(parameter.parameterName(), Map.of(
                        "type", jsonType(parameter.parameterType()),
                        "description", parameter.description() == null ? parameter.parameterName() : parameter.description()
                ));
                if (Integer.valueOf(1).equals(parameter.required())) {
                    required.add(parameter.parameterName());
                }
            }
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("properties", properties);
            schema.put("required", required);
            return schema;
        }

        /**
         * 把平台参数类型映射成 JSON Schema 基础类型。
         */
        private static String jsonType(String parameterType) {
            return switch ((parameterType == null ? "STRING" : parameterType).toUpperCase(Locale.ROOT)) {
                case "INTEGER", "LONG" -> "integer";
                case "DECIMAL" -> "number";
                case "BOOLEAN" -> "boolean";
                case "ARRAY" -> "array";
                case "OBJECT" -> "object";
                default -> "string";
            };
        }
    }
}
