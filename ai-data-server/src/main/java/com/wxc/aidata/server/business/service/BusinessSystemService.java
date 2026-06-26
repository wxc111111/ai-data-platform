package com.wxc.aidata.server.business.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.model.BusinessSystemCreateCommand;
import com.wxc.aidata.server.business.model.BusinessSystemPageQuery;
import com.wxc.aidata.server.business.model.BusinessSystemUpdateCommand;
import com.wxc.aidata.server.business.response.BusinessSystemResponse;

/**
 * 业务系统管理服务，封装外部系统基础信息和认证配置维护规则。
 */
public interface BusinessSystemService {

    /**
     * 分页查询业务系统列表。
     */
    PageResult<BusinessSystemResponse> pageBusinessSystems(BusinessSystemPageQuery query);

    /**
     * 查询业务系统详情。
     */
    BusinessSystemResponse getBusinessSystem(Long id);

    /**
     * 创建业务系统。
     */
    void createBusinessSystem(BusinessSystemCreateCommand command);

    /**
     * 更新业务系统。
     */
    void updateBusinessSystem(BusinessSystemUpdateCommand command);

    /**
     * 启用或禁用业务系统。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除业务系统。
     */
    void deleteBusinessSystem(Long id);
}
