package com.wxc.aidata.server;

import com.wxc.aidata.server.agent.config.AgentChatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * AI 数据服务后端启动类，负责启用 Spring Boot 和业务配置属性。
 */
@SpringBootApplication
@EnableConfigurationProperties(AgentChatProperties.class)
public class AiDataServerApplication {

    /**
     * 启动 AI 数据服务后端应用。
     */
    public static void main(String[] args) {
        SpringApplication.run(AiDataServerApplication.class, args);
    }
}
