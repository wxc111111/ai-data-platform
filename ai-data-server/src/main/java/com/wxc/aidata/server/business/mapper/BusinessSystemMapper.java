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

    /**
     * 查询业务系统授权角色 ID，用于详情回填和访问校验。
     */
    List<Long> findRoleIdsBySystemId(Long systemId);

    /**
     * 覆盖保存角色范围前先删除旧关系。
     */
    void deleteRolesBySystemId(Long systemId);

    /**
     * 批量插入业务系统角色范围。
     */
    void insertSystemRoles(@Param("systemId") Long systemId, @Param("roleIds") List<Long> roleIds);
}
