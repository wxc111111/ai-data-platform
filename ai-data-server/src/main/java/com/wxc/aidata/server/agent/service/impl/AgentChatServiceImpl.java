package com.wxc.aidata.server.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.agent.entity.AiChatMessage;
import com.wxc.aidata.server.agent.entity.AiChatSession;
import com.wxc.aidata.server.agent.mapper.AgentChatMessageMapper;
import com.wxc.aidata.server.agent.mapper.AgentChatSessionMapper;
import com.wxc.aidata.server.agent.model.AgentChatClientRequest;
import com.wxc.aidata.server.agent.model.AgentChatCommand;
import com.wxc.aidata.server.agent.response.AgentChatMessageResponse;
import com.wxc.aidata.server.agent.response.AgentChatResponse;
import com.wxc.aidata.server.agent.response.AgentChatSessionResponse;
import com.wxc.aidata.server.agent.response.AgentChatStreamEvent;
import com.wxc.aidata.server.agent.response.AgentUsedSkillResponse;
import com.wxc.aidata.server.agent.service.AgentChatClient;
import com.wxc.aidata.server.agent.service.AgentChatService;
import com.wxc.aidata.server.agent.service.AgentChatStreamHandler;
import com.wxc.aidata.server.agent.service.AgentChatStreamSink;
import com.wxc.aidata.server.common.id.IdGenerator;
import com.wxc.aidata.server.permission.model.ResourceAccessScope;
import com.wxc.aidata.server.permission.service.CurrentUserAccessService;
import com.wxc.aidata.server.skill.model.SkillPageQuery;
import com.wxc.aidata.server.skill.response.SkillResponse;
import com.wxc.aidata.server.skill.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 问答服务实现，负责会话持久化、权限校验、Skill 注入和流式响应编排。
 */
@Service
public class AgentChatServiceImpl implements AgentChatService {

    private static final int AGENT_ERROR_CODE = 4001;
    private static final int MAX_TITLE_LENGTH = 30;
    private static final TypeReference<List<AgentUsedSkillResponse>> USED_SKILL_LIST_TYPE = new TypeReference<>() {
    };

    private final AgentChatSessionMapper agentChatSessionMapper;
    private final AgentChatMessageMapper agentChatMessageMapper;
    private final SkillService skillService;
    private final AgentChatClient agentChatClient;
    private final CurrentUserAccessService currentUserAccessService;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    /**
     * 注入会话/消息 Mapper、Skill 管理、Agent 客户端和当前登录用户访问器。
     */
    public AgentChatServiceImpl(AgentChatSessionMapper agentChatSessionMapper,
                                AgentChatMessageMapper agentChatMessageMapper,
                                SkillService skillService,
                                AgentChatClient agentChatClient,
                                CurrentUserAccessService currentUserAccessService,
                                IdGenerator idGenerator) {
        this(agentChatSessionMapper, agentChatMessageMapper, skillService, agentChatClient,
                currentUserAccessService, idGenerator, new ObjectMapper());
    }

    /**
     * 注入带 ObjectMapper 的构造器，方便 Spring 复用全局 JSON 配置。
     */
    @Autowired
    public AgentChatServiceImpl(AgentChatSessionMapper agentChatSessionMapper,
                                AgentChatMessageMapper agentChatMessageMapper,
                                SkillService skillService,
                                AgentChatClient agentChatClient,
                                CurrentUserAccessService currentUserAccessService,
                                IdGenerator idGenerator,
                                ObjectMapper objectMapper) {
        this.agentChatSessionMapper = agentChatSessionMapper;
        this.agentChatMessageMapper = agentChatMessageMapper;
        this.skillService = skillService;
        this.agentChatClient = agentChatClient;
        this.currentUserAccessService = currentUserAccessService;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询当前用户历史会话，admin 允许查看全部便于运维排查。
     */
    @Override
    public List<AgentChatSessionResponse> listSessions() {
        Long userId = currentUserAccessService.currentUserId();
        boolean admin = currentUserAccessService.currentUserIsAdmin();
        List<AiChatSession> sessions = agentChatSessionMapper.findSessions(userId, admin);
        List<AgentChatSessionResponse> responses = new ArrayList<>();
        for (AiChatSession session : sessions) {
            responses.add(new AgentChatSessionResponse(
                    session.getId(),
                    session.getTitle(),
                    session.getLastMessageTime(),
                    session.getCreatedTime()
            ));
        }
        return responses;
    }

    /**
     * 读取会话消息前先校验归属，避免绕过前端直接访问其他用户会话。
     */
    @Override
    public List<AgentChatMessageResponse> listMessages(Long sessionId) {
        AiChatSession session = getAccessibleSession(sessionId);
        List<AiChatMessage> messages = agentChatMessageMapper.findMessagesBySessionId(session.getId());
        List<AgentChatMessageResponse> responses = new ArrayList<>();
        for (AiChatMessage message : messages) {
            responses.add(new AgentChatMessageResponse(
                    message.getId(),
                    message.getSessionId(),
                    message.getMessageRole(),
                    message.getContent(),
                    readUsedSkills(message.getUsedSkillsJson()),
                    message.getCreatedTime()
            ));
        }
        return responses;
    }

    /**
     * 发送消息时先落用户消息，再调用 AgentScope，最后保存助手完整回复和本轮 Skill 摘要。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentChatResponse chat(AgentChatCommand command) {
        validateCommand(command);
        // 在进入 AgentScope 前固化权限，工具异步执行时不再依赖当前 Web 请求。
        ResourceAccessScope accessScope = currentUserAccessService.currentAccessScope();
        Long userId = accessScope.userId();
        LocalDateTime now = LocalDateTime.now();
        AiChatSession session = command.sessionId() == null
                ? createSession(command.message(), userId, now)
                : getAccessibleSession(command.sessionId());
        saveMessage(session.getId(), userId, "USER", command.message(), List.of(), now);

        List<SkillResponse> skills = loadAvailableSkills();
        var agentResponse = agentChatClient.chat(new AgentChatClientRequest(
                userId, session.getId(), command.message(), skills, accessScope
        ));
        LocalDateTime completeTime = LocalDateTime.now();
        saveMessage(session.getId(), userId, "ASSISTANT", agentResponse.answer(), agentResponse.usedSkills(), completeTime);
        touchSession(session, userId, completeTime);
        return new AgentChatResponse(session.getId(), agentResponse.answer(), agentResponse.usedSkills());
    }

    /**
     * 流式发送消息时先把会话 ID 推给前端，再推送文本增量，完成后保存最终助手消息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void streamChat(AgentChatCommand command, AgentChatStreamSink sink) {
        validateCommand(command);
        // 流式问答同样只在 Web 线程读取一次用户权限，避免工具线程丢失请求上下文。
        ResourceAccessScope accessScope = currentUserAccessService.currentAccessScope();
        Long userId = accessScope.userId();
        LocalDateTime now = LocalDateTime.now();
        AiChatSession session = command.sessionId() == null
                ? createSession(command.message(), userId, now)
                : getAccessibleSession(command.sessionId());
        saveMessage(session.getId(), userId, "USER", command.message(), List.of(), now);
        sink.send(AgentChatStreamEvent.session(session.getId()));

        List<SkillResponse> skills = loadAvailableSkills();
        AgentChatClientRequest request = new AgentChatClientRequest(
                userId, session.getId(), command.message(), skills, accessScope
        );
        agentChatClient.stream(request, new PersistingStreamHandler(session, userId, sink));
    }

    /**
     * 从 Skill 列表加载当前用户有权访问且启用的 Skill，并补全参数定义给模型生成工具 schema。
     */
    private List<SkillResponse> loadAvailableSkills() {
        PageResult<SkillResponse> page = skillService.pageSkills(new SkillPageQuery(1, 200, null, null, 1));
        List<SkillResponse> skills = new ArrayList<>();
        for (SkillResponse skill : page.records()) {
            if (Integer.valueOf(1).equals(skill.status())) {
                skills.add(skillService.getSkill(skill.id()));
            }
        }
        return skills;
    }

    /**
     * 创建新会话，标题默认取第一条消息前 30 个字符。
     */
    private AiChatSession createSession(String message, Long userId, LocalDateTime now) {
        AiChatSession session = new AiChatSession(idGenerator.nextId(), userId, titleOf(message), now, now, userId, now, userId);
        agentChatSessionMapper.insert(session);
        return session;
    }

    /**
     * 保存一条聊天消息，助手消息会额外记录本轮 Skill 调用摘要。
     */
    private void saveMessage(Long sessionId, Long userId, String role, String content,
                             List<AgentUsedSkillResponse> usedSkills, LocalDateTime now) {
        agentChatMessageMapper.insert(new AiChatMessage(
                idGenerator.nextId(),
                sessionId,
                userId,
                role,
                content,
                writeUsedSkills(usedSkills),
                now,
                userId
        ));
    }

    /**
     * 使用 MyBatis-Plus 内置 updateById 刷新会话最后消息时间和审计字段。
     */
    private void touchSession(AiChatSession session, Long userId, LocalDateTime now) {
        session.setLastMessageTime(now);
        session.setUpdatedBy(userId);
        session.setUpdatedTime(now);
        agentChatSessionMapper.updateById(session);
    }

    /**
     * 校验会话归属，admin 可查看全部历史会话。
     */
    private AiChatSession getAccessibleSession(Long sessionId) {
        if (sessionId == null) {
            throw new BusinessException(AGENT_ERROR_CODE, "会话 ID 不能为空");
        }
        AiChatSession session = agentChatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(AGENT_ERROR_CODE, "会话不存在");
        }
        Long userId = currentUserAccessService.currentUserId();
        if (!currentUserAccessService.currentUserIsAdmin() && !session.getUserId().equals(userId)) {
            throw new BusinessException(AGENT_ERROR_CODE, "无权访问该会话");
        }
        return session;
    }

    /**
     * 校验用户输入，防止空消息进入模型调用和历史记录。
     */
    private void validateCommand(AgentChatCommand command) {
        if (command == null || command.message() == null || command.message().isBlank()) {
            throw new BusinessException(AGENT_ERROR_CODE, "消息内容不能为空");
        }
    }

    /**
     * 生成会话标题，避免左侧列表标题过长。
     */
    private String titleOf(String message) {
        String trimmed = message.trim();
        return trimmed.length() <= MAX_TITLE_LENGTH ? trimmed : trimmed.substring(0, MAX_TITLE_LENGTH);
    }

    /**
     * 序列化本轮 Skill 调用摘要，便于历史消息回显。
     */
    private String writeUsedSkills(List<AgentUsedSkillResponse> usedSkills) {
        try {
            return objectMapper.writeValueAsString(usedSkills == null ? List.of() : usedSkills);
        } catch (JsonProcessingException e) {
            throw new BusinessException(AGENT_ERROR_CODE, "Skill 调用记录序列化失败");
        }
    }

    /**
     * 读取历史 Skill 调用摘要，旧数据为空或格式异常时按空列表处理。
     */
    private List<AgentUsedSkillResponse> readUsedSkills(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, USED_SKILL_LIST_TYPE);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 流式回调处理器，汇总最终答案并在完成时落库。
     */
    private final class PersistingStreamHandler implements AgentChatStreamHandler {

        private final AiChatSession session;
        private final Long userId;
        private final AgentChatStreamSink sink;
        private final StringBuilder answer = new StringBuilder();

        private PersistingStreamHandler(AiChatSession session, Long userId, AgentChatStreamSink sink) {
            this.session = session;
            this.userId = userId;
            this.sink = sink;
        }

        @Override
        public void onText(String delta) {
            if (delta == null || delta.isEmpty()) {
                return;
            }
            answer.append(delta);
            sink.send(AgentChatStreamEvent.delta(session.getId(), delta));
        }

        @Override
        public void onComplete(String finalAnswer, List<AgentUsedSkillResponse> usedSkills) {
            String content = finalAnswer == null || finalAnswer.isBlank() ? answer.toString() : finalAnswer;
            LocalDateTime completeTime = LocalDateTime.now();
            saveMessage(session.getId(), userId, "ASSISTANT", content, usedSkills, completeTime);
            touchSession(session, userId, completeTime);
            sink.send(AgentChatStreamEvent.done(session.getId(), usedSkills));
        }
    }
}
