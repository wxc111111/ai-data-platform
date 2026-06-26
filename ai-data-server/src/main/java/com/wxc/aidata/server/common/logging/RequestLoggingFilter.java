package com.wxc.aidata.server.common.logging;

import com.wxc.aidata.common.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 请求日志过滤器，统一生成和透传 requestId，便于接口响应与日志关联排查。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 包装每次 HTTP 请求，在请求链路前后维护日志上下文并输出访问日志。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = resolveRequestId(request);

        // 将 requestId 同步到业务响应上下文和日志 MDC，保证同一次请求可被串联追踪。
        RequestContext.setRequestId(requestId);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long costMs = System.currentTimeMillis() - startTime;

            // 只记录路径，不记录 query string，避免把敏感查询参数写入日志。
            log.info("http_request requestId={} method={} path={} status={} costMs={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    costMs);

            MDC.remove(MDC_REQUEST_ID_KEY);
            RequestContext.clear();
        }
    }

    /**
     * 优先复用调用方传入的链路标识；缺失时生成新的 UUID。
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId.trim();
    }
}
