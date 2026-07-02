package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 业务接口分页查询条件，封装前端列表筛选字段。
 */
public record BusinessApiPageQuery(
        Integer pageNo,
        Integer pageSize,
        Long systemId,
        String apiName,
        String apiCode,
        Integer status,
        List<Long> roleIds,
        Boolean admin
) {

    public BusinessApiPageQuery(Integer pageNo, Integer pageSize, Long systemId, String apiName, String apiCode, Integer status) {
        this(pageNo, pageSize, systemId, apiName, apiCode, status, List.of(), false);
    }

    /**
     * 补充当前用户角色范围，普通用户只能看到命中角色范围的接口。
     */
    public BusinessApiPageQuery withAccessScope(List<Long> roleIds, boolean admin) {
        return new BusinessApiPageQuery(pageNo, pageSize, systemId, apiName, apiCode, status, roleIds == null ? List.of() : roleIds, admin);
    }

    /**
     * 规范化页码，避免空值或非法值影响分页插件。
     */
    public Integer normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 规范化页大小，限制单页最大 100 条。
     */
    public Integer normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
