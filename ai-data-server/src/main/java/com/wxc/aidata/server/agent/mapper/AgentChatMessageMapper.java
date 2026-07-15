package com.wxc.aidata.server.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.agent.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 问答消息 Mapper，消息新增使用 MyBatis-Plus 内置 CRUD，历史查询使用 XML SQL 保持排序清晰。
 */
@Mapper
public interface AgentChatMessageMapper extends BaseMapper<AiChatMessage> {

    /**
     * 查询某个会话下的全部历史消息。
     */
    List<AiChatMessage> findMessagesBySessionId(Long sessionId);
}
