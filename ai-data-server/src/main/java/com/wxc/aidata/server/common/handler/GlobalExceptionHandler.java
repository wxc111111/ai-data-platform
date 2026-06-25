package com.wxc.aidata.server.common.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，直接使用业务层给出的错误码和提示信息。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理 Sa-Token 未登录异常，统一返回登录失效提示。
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException exception) {
        return Result.failure(10002, "登录已失效");
    }
}
