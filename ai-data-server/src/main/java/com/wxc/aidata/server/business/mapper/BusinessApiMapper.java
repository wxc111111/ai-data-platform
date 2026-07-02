package com.wxc.aidata.server.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.business.entity.BizApi;
import com.wxc.aidata.server.business.model.BusinessApiPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务接口数据库访问接口，简单主键操作复用 MyBatis-Plus，复杂查询交给 XML SQL。
 */
@Mapper
public interface BusinessApiMapper extends BaseMapper<BizApi> {

    /**
     * 按业务系统、名称、编码和状态分页筛选业务接口。
     */
    List<BusinessApiRow> findBusinessApis(@Param("query") BusinessApiPageQuery query);

    /**
     * 查询接口编码是否已存在，用于新增校验。
     */
    boolean existsByApiCode(String apiCode);

    /**
     * 查询除当前 ID 外是否存在相同编码，用于编辑校验。
     */
    boolean existsByApiCodeExcludeId(@Param("apiCode") String apiCode, @Param("id") Long id);

    /**
     * 查询业务接口授权角色 ID，用于详情回填和访问校验。
     */
    List<Long> findRoleIdsByApiId(Long apiId);

    /**
     * 覆盖保存角色范围前先删除旧关系。
     */
    void deleteRolesByApiId(Long apiId);

    /**
     * 批量插入业务接口角色范围。
     */
    void insertApiRoles(@Param("apiId") Long apiId, @Param("roleIds") List<Long> roleIds);

    /**
     * 分页查询行对象，额外携带业务系统名称。
     */
    record BusinessApiRow(
            Long id,
            Long systemId,
            String systemName,
            String systemBaseUrl,
            String apiCode,
            String apiName,
            String requestPath,
            String requestMethod,
            String contentType,
            Integer connectTimeout,
            Integer readTimeout,
            String responseDataPath,
            Integer status,
            String description,
            Long createdBy,
            LocalDateTime createdTime,
            Long updatedBy,
            LocalDateTime updatedTime
    ) {
        public BusinessApiRow(Long id, Long systemId, String systemName, String systemBaseUrl, String apiCode,
                              String apiName, String requestPath, String requestMethod, String contentType,
                              Integer connectTimeout, Integer readTimeout, String responseDataPath,
                              Integer status, String description, LocalDateTime createdTime, LocalDateTime updatedTime) {
            this(id, systemId, systemName, systemBaseUrl, apiCode, apiName, requestPath, requestMethod, contentType,
                    connectTimeout, readTimeout, responseDataPath, status, description, null, createdTime, null, updatedTime);
        }
    }
}
