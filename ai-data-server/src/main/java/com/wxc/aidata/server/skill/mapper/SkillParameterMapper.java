package com.wxc.aidata.server.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.skill.entity.AiSkillParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Skill 参数映射数据库访问接口。
 */
@Mapper
public interface SkillParameterMapper extends BaseMapper<AiSkillParameter> {

    /**
     * 查询指定 Skill 的参数映射，按排序号稳定返回。
     */
    List<AiSkillParameter> findBySkillId(Long skillId);

    /**
     * 删除指定 Skill 的全部参数映射，用于覆盖保存。
     */
    int deleteBySkillId(@Param("skillId") Long skillId);
}
