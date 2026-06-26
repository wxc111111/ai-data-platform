package com.wxc.aidata.server.business.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.server.business.service.BusinessApiHttpClient;
import com.wxc.aidata.server.business.service.BusinessApiHttpRequest;
import com.wxc.aidata.server.business.service.BusinessApiHttpResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * JDK HttpClient 实现，用于在线测试第三方业务接口。
 */
@Component
public class JavaBusinessApiHttpClient implements BusinessApiHttpClient {

    private static final int BUSINESS_API_ERROR_CODE = 12002;

    /**
     * 发送 HTTP 请求并统计耗时，网络异常统一转换为业务异常。
     */
    @Override
    public BusinessApiHttpResponse exchange(BusinessApiHttpRequest request) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(request.connectTimeout()))
                    .build();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(request.url()))
                    .timeout(Duration.ofMillis(request.readTimeout()));
            request.headers().forEach(builder::header);

            HttpRequest.BodyPublisher bodyPublisher = request.body() == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(request.body());
            builder.method(request.method(), bodyPublisher);

            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new BusinessApiHttpResponse(
                    response.statusCode(),
                    response.headers().map(),
                    response.body(),
                    System.currentTimeMillis() - start
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口测试被中断");
        } catch (RuntimeException | java.io.IOException exception) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口测试失败：" + exception.getMessage());
        }
    }
}
