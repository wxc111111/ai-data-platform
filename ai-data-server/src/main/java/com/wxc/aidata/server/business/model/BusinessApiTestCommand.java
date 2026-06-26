package com.wxc.aidata.server.business.model;

import java.util.Map;

/**
 * 业务接口在线测试命令，携带本次测试输入参数。
 */
public record BusinessApiTestCommand(
        Map<String, Object> parameterValues
) {
}
