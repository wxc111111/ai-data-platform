package com.wxc.aidata.server.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.agent.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 问答会话 Mapper，单条新增/查询/更新使用 MyBatis-Plus 内置 CRUD，列表查询交给 XML SQL。
 */
@Mapper
public interface AgentChatSessionMapper extends BaseMapper<AiChatSession> {

    /**
     * 查询当前用户可见会话，管理员可查看全部会话用于排查。
     */
    List<AiChatSession> findSessions(@Param("userId") Long userId, @Param("admin") boolean admin);
}
