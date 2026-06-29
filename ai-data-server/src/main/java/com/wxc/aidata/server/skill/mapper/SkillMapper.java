package com.wxc.aidata.server.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.skill.entity.AiSkill;
import com.wxc.aidata.server.skill.model.SkillPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Skill 数据库访问接口，复杂列表查询交给 XML SQL。
 */
@Mapper
public interface SkillMapper extends BaseMapper<AiSkill> {

    /**
     * 按 Skill 名称、编码和状态分页查询 Skill 列表。
     */
    List<SkillRow> findSkills(@Param("query") SkillPageQuery query);

    /**
     * 新增前校验 Skill 编码唯一性。
     */
    boolean existsBySkillCode(String skillCode);

    /**
     * 编辑前校验除当前 ID 外的 Skill 编码唯一性。
     */
    boolean existsBySkillCodeExcludeId(@Param("skillCode") String skillCode, @Param("id") Long id);

    /**
     * Skill 列表行，额外携带关联业务接口名称和编码。
     */
    record SkillRow(
            Long id,
            String skillCode,
            String skillName,
            String description,
            Long apiId,
            String apiName,
            String apiCode,
            String permissionCode,
            Integer timeoutMs,
            Integer maxResultCount,
            Integer status,
            Integer versionNo,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }
}
