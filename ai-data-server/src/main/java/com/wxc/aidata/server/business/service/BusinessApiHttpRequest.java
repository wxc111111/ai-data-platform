package com.wxc.aidata.server.business.service;

import java.util.Map;

/**
 * 业务接口 HTTP 请求对象，隔离在线测试的请求组装和实际发送。
 */
public record BusinessApiHttpRequest(
        String method,
        String url,
        Map<String, String> headers,
        String body,
        Integer connectTimeout,
        Integer readTimeout
) {
}
