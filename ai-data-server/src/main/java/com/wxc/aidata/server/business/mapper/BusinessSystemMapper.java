package com.wxc.aidata.server.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.business.entity.BizSystem;
import com.wxc.aidata.server.business.model.BusinessSystemPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务系统数据库访问接口，简单主键操作复用 MyBatis-Plus，复杂查询交给 XML SQL。
 */
@Mapper
public interface BusinessSystemMapper extends BaseMapper<BizSystem> {

    /**
     * 按名称、编码和状态分页筛选业务系统。
     */
    List<BizSystem> findBusinessSystems(@Param("query") BusinessSystemPageQuery query);

    /**
     * 查询业务系统编码是否已存在，用于新增校验。
     */
    boolean existsBySystemCode(String systemCode);

    /**
     * 查询除当前 ID 外是否存在相同编码，用于编辑校验。
     */
    boolean existsBySystemCodeExcludeId(@Param("systemCode") String systemCode, @Param("id") Long id);
}
