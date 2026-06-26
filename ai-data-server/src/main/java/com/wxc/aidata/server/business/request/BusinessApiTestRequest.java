package com.wxc.aidata.server.business.request;

import java.util.Map;

/**
 * 业务接口在线测试请求，携带本次测试参数值。
 */
public record BusinessApiTestRequest(
        Map<String, Object> parameterValues
) {
}
