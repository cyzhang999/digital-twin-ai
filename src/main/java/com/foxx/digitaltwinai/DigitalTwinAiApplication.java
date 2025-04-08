package com.foxx.digitaltwinai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 数字孪生AI应用程序主类
 * (Digital Twin AI Application Main Class)
 */
@SpringBootApplication
@EnableConfigurationProperties
public class DigitalTwinAiApplication {

    /**
     * 主方法
     * (Main Method)
     *
     * @param args 命令行参数 (Command Line Arguments)
     */
    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinAiApplication.class, args);
    }
} 