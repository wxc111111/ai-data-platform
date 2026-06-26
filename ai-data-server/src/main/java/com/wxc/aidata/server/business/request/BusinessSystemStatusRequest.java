package com.wxc.aidata.server.business.request;

/**
 * 业务系统状态更新请求体，用于启用或禁用业务系统。
 */
public record BusinessSystemStatusRequest(Integer status) {
}
