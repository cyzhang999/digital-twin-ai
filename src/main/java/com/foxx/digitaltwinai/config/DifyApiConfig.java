package com.foxx.digitaltwinai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Dify API配置类
 * (Dify API Configuration Class)
 */
@Configuration
@ConfigurationProperties(prefix = "dify.api")
@Data
public class DifyApiConfig {
    
    /**
     * Dify API基础URL
     * (Dify API Base URL)
     */
    private String baseUrl;
    
    /**
     * API版本
     * (API Version)
     */
    private String version;
    
    /**
     * 聊天消息接口路径
     * (Chat Messages Endpoint)
     */
    private String chatEndpoint;
    
    /**
     * Dify API密钥
     * (Dify API Key)
     */
    private String apiKey;
    
    /**
     * 服务名称
     * (Service Name)
     */
    private String serviceName;
    
    /**
     * 连接超时（毫秒）
     * (Connection Timeout in milliseconds)
     */
    private int connectTimeout = 30000;  // 默认30秒
    
    /**
     * 读取超时（毫秒）
     * (Read Timeout in milliseconds)
     */
    private int readTimeout = 60000;  // 默认60秒
    
    /**
     * HMAC签名配置
     * (HMAC Signature Configuration)
     */
    private Hmac hmac = new Hmac();
    
    /**
     * 本地回退配置
     * (Local Fallback Configuration)
     */
    private FallbackConfig fallback = new FallbackConfig();
    
    /**
     * HMAC签名配置内部类
     * (HMAC Signature Configuration Inner Class)
     */
    @Data
    public static class Hmac {
        /**
         * 是否启用HMAC签名
         * (Enable HMAC Signature)
         */
        private boolean enabled;
        
        /**
         * HMAC密钥
         * (HMAC Secret Key)
         */
        private String secretKey;
    }
    
    /**
     * 获取完整的聊天消息API URL
     * (Get Full Chat Messages API URL)
     */
    public String getChatMessagesUrl() {
        return String.format("%s/%s%s", baseUrl, version, chatEndpoint);
    }
    
    /**
     * HMAC配置类
     * (HMAC Configuration Class)
     */
    @Data
    public static class HmacConfig {
        
        /**
         * 是否启用HMAC
         * (Whether HMAC is Enabled)
         */
        private boolean enabled;
        
        /**
         * HMAC密钥
         * (HMAC Secret Key)
         */
        private String secretKey;
    }
    
    /**
     * 本地回退配置类
     * (Local Fallback Configuration Class)
     */
    @Data
    public static class FallbackConfig {
        
        /**
         * 是否启用本地回退
         * (Whether Local Fallback is Enabled)
         */
        private boolean enabled = true;
        
        /**
         * 是否在API调用失败时自动重试
         * (Whether to Auto Retry on API Call Failure)
         */
        private boolean autoRetryOnFailure = true;
    }
} 