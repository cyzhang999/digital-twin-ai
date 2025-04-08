package com.foxx.digitaltwinai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson全局配置类
 * (Jackson Global Configuration)
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置全局ObjectMapper
     * (Configure global ObjectMapper)
     *
     * @return 配置好的ObjectMapper (Configured ObjectMapper)
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块 (Register Java 8 time module)
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}