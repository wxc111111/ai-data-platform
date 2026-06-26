package com.wxc.aidata.server.audit.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析工具，优先读取代理透传头，缺失时回退到远端地址。
 */
public final class ClientIpResolver {

    private static final String UNKNOWN = "unknown";

    private ClientIpResolver() {
    }

    /**
     * 解析客户端 IP，支持常见反向代理头。
     */
    public static String resolve(HttpServletRequest request) {
        String xForwardedFor = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (xForwardedFor != null) {
            return xForwardedFor;
        }

        String realIp = firstValidIp(request.getHeader("X-Real-IP"));
        if (realIp != null) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 从代理头中提取第一个有效 IP。
     */
    private static String firstValidIp(String value) {
        if (value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value)) {
            return null;
        }
        String first = value.split(",")[0].trim();
        return first.isBlank() || UNKNOWN.equalsIgnoreCase(first) ? null : first;
    }
}
