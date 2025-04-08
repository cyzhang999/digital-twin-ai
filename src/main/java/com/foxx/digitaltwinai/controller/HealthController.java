package com.foxx.digitaltwinai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * (Health Check Controller)
 */
@RestController
public class HealthController {

    /**
     * 健康检查端点
     * (Health check endpoint)
     *
     * @return 系统状态信息 (System status information)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "up");
        status.put("service", "digital-twin-ai");
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }
}