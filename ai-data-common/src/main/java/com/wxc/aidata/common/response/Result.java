package com.wxc.aidata.common.response;

import com.wxc.aidata.common.context.RequestContext;

public record Result<T>(
        Integer code,
        String message,
        T data,
        String requestId
) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, RequestContext.getRequestId());
    }

    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null, RequestContext.getRequestId());
    }
}
