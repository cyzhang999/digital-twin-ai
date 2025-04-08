package com.foxx.digitaltwinai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RestTemplate配置类
 * (RestTemplate Configuration Class)
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {
    
    /**
     * Dify API配置
     * (Dify API Configuration)
     */
    private final DifyApiConfig difyApiConfig;
    
    /**
     * 浏览器服务配置
     * (Browser Service Configuration)
     */
    private final BrowserServiceConfig browserServiceConfig;
    
    /**
     * 创建RestTemplate Bean
     * (Create RestTemplate Bean)
     * 
     * @return RestTemplate实例 (RestTemplate instance)
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        
        // 添加支持text/event-stream内容类型的转换器
        // (Add converter to support text/event-stream content type)
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(restTemplate.getMessageConverters());
        
        // 创建支持text/event-stream的Jackson转换器
        // (Create Jackson converter supporting text/event-stream)
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        // 添加对SSE内容类型的支持
        // (Add support for SSE content type)
        mediaTypes.add(MediaType.valueOf("text/event-stream;charset=UTF-8"));
        converter.setSupportedMediaTypes(mediaTypes);
        
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
        
        return restTemplate;
    }
    
    /**
     * 创建ClientHttpRequestFactory，配置超时时间
     * (Create ClientHttpRequestFactory with timeout configuration)
     * 
     * @return ClientHttpRequestFactory实例 (ClientHttpRequestFactory instance)
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 从配置中读取超时设置 (Read timeout settings from configuration)
        // 两者中取较大值，便于处理长时间运行的请求
        factory.setConnectTimeout(
            Math.max(difyApiConfig.getConnectTimeout(), browserServiceConfig.getConnectionTimeout())
        );
        factory.setReadTimeout(
            Math.max(difyApiConfig.getReadTimeout(), browserServiceConfig.getReadTimeout())
        );
        
        return factory;
    }
} 