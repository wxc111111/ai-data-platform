package com.wxc.aidata.server.business.service;

/**
 * 业务接口 HTTP 客户端，便于在线测试逻辑替换为测试桩。
 */
public interface BusinessApiHttpClient {

    /**
     * 发送在线测试请求并返回响应。
     */
    BusinessApiHttpResponse exchange(BusinessApiHttpRequest request);
}
