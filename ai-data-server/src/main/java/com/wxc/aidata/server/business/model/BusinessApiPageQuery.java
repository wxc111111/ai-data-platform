package com.wxc.aidata.server.business.model;

/**
 * 业务接口分页查询条件，封装前端列表筛选字段。
 */
public record BusinessApiPageQuery(
        Integer pageNo,
        Integer pageSize,
        Long systemId,
        String apiName,
        String apiCode,
        Integer status
) {

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
