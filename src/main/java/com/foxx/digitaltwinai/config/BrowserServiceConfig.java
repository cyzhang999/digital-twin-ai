package com.foxx.digitaltwinai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 浏览器服务配置类
 * (Browser Service Configuration Class)
 */
@Configuration
@ConfigurationProperties(prefix = "browser-service")
@Data
public class BrowserServiceConfig {
    
    /**
     * 浏览器服务基础URL
     * (Browser Service Base URL)
     */
    private String url = "http://localhost:9000";
    
    /**
     * 连接超时（毫秒）
     * (Connection Timeout in milliseconds)
     */
    private int connectionTimeout = 10000;  // 默认10秒
    
    /**
     * 读取超时（毫秒）
     * (Read Timeout in milliseconds)
     */
    private int readTimeout = 30000;  // 默认30秒
} 