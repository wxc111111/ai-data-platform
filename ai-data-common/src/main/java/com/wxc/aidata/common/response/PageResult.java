package com.wxc.aidata.common.response;

import java.util.List;

/**
 * 分页响应结构，统一后台列表接口的返回格式。
 */
public record PageResult<T>(
        Long total,
        Integer pageNo,
        Integer pageSize,
        List<T> records
) {

    /**
     * 创建分页结果，空列表由调用方显式传入，避免返回 null。
     */
    public static <T> PageResult<T> of(Long total, Integer pageNo, Integer pageSize, List<T> records) {
        return new PageResult<>(total, pageNo, pageSize, records);
    }
}
