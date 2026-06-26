package com.wxc.aidata.server.audit.model;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询条件，封装前端筛选字段和分页参数。
 */
public record LoginLogPageQuery(
        Integer pageNo,
        Integer pageSize,
        String username,
        String loginStatus,
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    /**
     * 规范化页码，避免非法页码传入分页插件。
     */
    public int normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 规范化每页数量，限制最大 100 条，避免一次查询过多日志。
     */
    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
