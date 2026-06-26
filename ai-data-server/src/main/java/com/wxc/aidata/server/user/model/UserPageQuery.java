package com.wxc.aidata.server.user.model;

/**
 * 用户分页查询条件，支持按账号、手机号和状态筛选。
 */
public record UserPageQuery(
        Integer pageNo,
        Integer pageSize,
        String username,
        String mobile,
        Integer status
) {

    /**
     * 规范化页码，避免非法页码进入 SQL。
     */
    public int normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 规范化分页大小，最大限制为 100。
     */
    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
