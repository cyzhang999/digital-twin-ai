package com.foxx.digitaltwinai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

/**
 * Web配置类，用于配置CORS等Web相关设置
 * Web configuration for CORS and other web-related settings
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置CORS，允许前端跨域访问API
     * Configure CORS to allow frontend cross-domain access to API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")  // 前端开发服务器地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // 1小时内不再预检
        
        // 记录日志
        System.out.println("CORS配置已加载，允许来自 http://localhost:3000 的跨域请求");
    }

    /**
     * 配置RestTemplate用于HTTP请求
     * (Configure RestTemplate for HTTP Requests)
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(10000))
                .build();
    }
} 