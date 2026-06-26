package com.wxc.aidata.server.business.model;

/**
 * 业务系统分页查询条件，支持按名称、编码和状态筛选。
 */
public record BusinessSystemPageQuery(
        Integer pageNo,
        Integer pageSize,
        String systemName,
        String systemCode,
        Integer status
) {

    /**
     * 规范化页码，避免非法页码进入分页插件。
     */
    public int normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 规范化每页数量，最大限制为 100。
     */
    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
