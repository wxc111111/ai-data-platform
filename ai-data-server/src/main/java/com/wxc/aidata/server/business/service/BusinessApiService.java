package com.wxc.aidata.server.business.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.model.BusinessApiCreateCommand;
import com.wxc.aidata.server.business.model.BusinessApiPageQuery;
import com.wxc.aidata.server.business.model.BusinessApiTestCommand;
import com.wxc.aidata.server.business.model.BusinessApiUpdateCommand;
import com.wxc.aidata.server.business.response.BusinessApiResponse;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;

/**
 * 业务接口管理服务，封装第三方接口配置、参数维护和在线测试规则。
 */
public interface BusinessApiService {

    /**
     * 分页查询业务接口列表。
     */
    PageResult<BusinessApiResponse> pageBusinessApis(BusinessApiPageQuery query);

    /**
     * 查询业务接口详情。
     */
    BusinessApiResponse getBusinessApi(Long id);

    /**
     * 创建业务接口。
     */
    void createBusinessApi(BusinessApiCreateCommand command);

    /**
     * 更新业务接口。
     */
    void updateBusinessApi(BusinessApiUpdateCommand command);

    /**
     * 启用或禁用业务接口。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除业务接口。
     */
    void deleteBusinessApi(Long id);

    /**
     * 按当前接口配置发起一次在线测试。
     */
    BusinessApiTestResponse testBusinessApi(Long id, BusinessApiTestCommand command);
}
