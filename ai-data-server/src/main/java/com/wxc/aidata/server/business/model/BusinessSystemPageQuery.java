package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 业务系统分页查询条件，支持按名称、编码和状态筛选。
 */
public record BusinessSystemPageQuery(
        Integer pageNo,
        Integer pageSize,
        String systemName,
        String systemCode,
        Integer status,
        List<Long> roleIds,
        Boolean admin
) {

    public BusinessSystemPageQuery(Integer pageNo, Integer pageSize, String systemName, String systemCode, Integer status) {
        this(pageNo, pageSize, systemName, systemCode, status, List.of(), false);
    }

    /**
     * 补充当前用户角色范围，避免控制器感知资源裁剪细节。
     */
    public BusinessSystemPageQuery withAccessScope(List<Long> roleIds, boolean admin) {
        return new BusinessSystemPageQuery(pageNo, pageSize, systemName, systemCode, status, roleIds == null ? List.of() : roleIds, admin);
    }

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
