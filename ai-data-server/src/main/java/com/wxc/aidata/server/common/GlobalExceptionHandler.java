package com.wxc.aidata.server.common;

import cn.dev33.satoken.exception.NotLoginException;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException exception) {
        return Result.failure(10002, "登录已失效");
    }
}
