package com.wxc.aidata.server.role.model;

/**
 * 角色分页查询条件，封装页面传入的筛选和分页参数。
 */
public record RolePageQuery(
        Integer pageNo,
        Integer pageSize,
        String roleCode,
        Integer status
) {

    /**
     * 规范化页码，避免前端传空或非法值导致分页异常。
     */
    public Integer normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 规范化每页条数，限制最大值避免一次查询过多数据。
     */
    public Integer normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
