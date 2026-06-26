package com.wxc.aidata.server.audit.web;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 请求参数脱敏工具，只处理 query/form 参数，不读取请求体，避免影响业务流读取 body。
 */
final class RequestParamSanitizer {

    private static final String MASK = "******";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "token",
            "authorization",
            "secret",
            "credential",
            "mysqlPassword",
            "redisPassword"
    );

    private RequestParamSanitizer() {
    }

    /**
     * 序列化请求参数，并对敏感字段做固定掩码。
     */
    static String sanitize(HttpServletRequest request) {
        if (request.getParameterMap().isEmpty()) {
            return null;
        }

        return request.getParameterMap()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "=" + sanitizeValue(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * 对敏感参数统一脱敏，普通参数保留原始值便于排查。
     */
    private static String sanitizeValue(String key, String[] values) {
        if (isSensitiveKey(key)) {
            return MASK;
        }
        return Arrays.stream(values == null ? new String[0] : values)
                .collect(Collectors.joining(","));
    }

    /**
     * 判断参数名是否属于敏感字段。
     */
    private static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.stream().anyMatch(item -> item.equalsIgnoreCase(key));
    }
}
