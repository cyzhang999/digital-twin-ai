package com.foxx.digitaltwinai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxx.digitaltwinai.config.BrowserServiceConfig;
import com.foxx.digitaltwinai.service.BrowserService;
import com.foxx.digitaltwinai.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 浏览器服务实现类
 * (Browser Service Implementation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowserServiceImpl implements BrowserService {
    
    /**
     * 浏览器服务配置
     * (Browser Service Configuration)
     */
    private final BrowserServiceConfig browserServiceConfig;
    
    /**
     * RestTemplate
     */
    private final RestTemplate restTemplate;
    
    /**
     * ObjectMapper
     */
    private final ObjectMapper objectMapper;
    
    /**
     * WebSocket服务
     * (WebSocket Service)
     */
    private final WebSocketService webSocketService;
    
    /**
     * 执行模型操作
     * (Execute model operation)
     */
    @Override
    public Map<String, Object> executeModelOperation(Map<String, Object> action) {
        try {
            if (action == null) {
                throw new IllegalArgumentException("操作数据不能为空 (Action data cannot be null)");
            }
            
            // 获取操作类型 (Get operation type)
            String operationType = (String) action.get("type");
            if (operationType == null) {
                throw new IllegalArgumentException("操作类型不能为空 (Operation type cannot be null)");
            }
            
            // 获取操作参数 (Get operation parameters)
            Map<String, Object> parameters = action.containsKey("params") ? 
                    (Map<String, Object>) action.get("params") : new HashMap<>();
            
            // 根据操作类型执行相应的操作 (Execute operation based on operation type)
            log.info("执行操作: {}, 参数: {} (Executing operation: {}, parameters: {})", 
                    operationType, parameters, operationType, parameters);
            
            switch (operationType) {
                case "rotate":
                    return executeRotate(parameters);
                case "zoom":
                    return executeZoom(parameters);
                case "focus":
                    String target = parameters.containsKey("target") ? 
                            (String) parameters.get("target") : "center";
                    return executeFocus(target);
                case "reset":
                    return executeReset();
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType + 
                            " (Unsupported operation type: " + operationType + ")");
            }
        } catch (Exception e) {
            log.error("执行模型操作失败 (Failed to execute model operation)", e);
            
            // 发送WebSocket错误消息 (Send WebSocket error message)
            webSocketService.sendError("执行模型操作失败: " + e.getMessage() + 
                    " (Failed to execute model operation: " + e.getMessage() + ")");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行模型操作失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 执行旋转操作
     * (Execute rotation operation)
     */
    @Override
    public Map<String, Object> executeRotate(Map<String, Object> parameters) {
        try {
            // 确保参数不为空
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            // 获取旋转参数
            String direction = parameters.containsKey("direction") ? 
                               (String) parameters.get("direction") : "left";
            
            Number angleNumber = (Number) parameters.getOrDefault("angle", 30);
            int angle = angleNumber.intValue();
            
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", "rotate");
            requestBody.put("parameters", parameters);
            
            // 发送请求
            Map<String, Object> result = sendRequest(requestBody);
            log.info("旋转操作执行结果: {} (Rotate operation result: {})", result, result);
            
            // 发送WebSocket消息
            webSocketService.sendLog("旋转操作执行完成，方向: " + direction + ", 角度: " + angle + 
                    " (Rotate operation completed, direction: " + direction + ", angle: " + angle + ")");
            
            return result;
        } catch (Exception e) {
            log.error("执行旋转操作失败 (Failed to execute rotate operation)", e);
            
            // 发送WebSocket错误消息
            webSocketService.sendError("旋转操作失败: " + e.getMessage() + 
                    " (Rotate operation failed: " + e.getMessage() + ")");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行旋转操作失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 执行缩放操作
     * (Execute zoom operation)
     */
    @Override
    public Map<String, Object> executeZoom(Map<String, Object> parameters) {
        try {
            // 确保参数不为空
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            // 获取缩放参数
            Number scaleNumber = (Number) parameters.getOrDefault("scale", 1.5);
            float scale = scaleNumber.floatValue();
            
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", "zoom");
            requestBody.put("parameters", Collections.singletonMap("scale", scale));
            
            // 发送请求
            Map<String, Object> result = sendRequest(requestBody);
            log.info("缩放操作执行结果: {} (Zoom operation result: {})", result, result);
            
            // 发送WebSocket消息
            webSocketService.sendLog("缩放操作执行完成，比例: " + scale + 
                    " (Zoom operation completed, scale: " + scale + ")");
            
            return result;
        } catch (Exception e) {
            log.error("执行缩放操作失败 (Failed to execute zoom operation)", e);
            
            // 发送WebSocket错误消息
            webSocketService.sendError("缩放操作失败: " + e.getMessage() + 
                    " (Zoom operation failed: " + e.getMessage() + ")");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行缩放操作失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 执行聚焦操作
     * (Execute focus operation)
     */
    @Override
    public Map<String, Object> executeFocus(String target) {
        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", "focus");
            requestBody.put("parameters", Collections.singletonMap("target", target));
            
            // 发送请求
            Map<String, Object> result = sendRequest(requestBody);
            log.info("聚焦操作执行结果: {} (Focus operation result: {})", result, result);
            
            // 发送WebSocket消息
            webSocketService.sendLog("聚焦操作执行完成，目标: " + target + 
                    " (Focus operation completed, target: " + target + ")");
            
            return result;
        } catch (Exception e) {
            log.error("执行聚焦操作失败 (Failed to execute focus operation)", e);
            
            // 发送WebSocket错误消息
            webSocketService.sendError("聚焦操作失败: " + e.getMessage() + 
                    " (Focus operation failed: " + e.getMessage() + ")");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行聚焦操作失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 执行重置操作
     * (Execute reset operation)
     */
    @Override
    public Map<String, Object> executeReset() {
        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", "reset");
            requestBody.put("parameters", Collections.emptyMap());
            
            // 发送请求
            Map<String, Object> result = sendRequest(requestBody);
            log.info("重置操作执行结果: {} (Reset operation result: {})", result, result);
            
            // 发送WebSocket消息
            webSocketService.sendLog("视图重置操作执行完成 (View reset operation completed)");
            
            return result;
        } catch (Exception e) {
            log.error("执行重置操作失败 (Failed to execute reset operation)", e);
            
            // 发送WebSocket错误消息
            webSocketService.sendError("重置操作失败: " + e.getMessage() + 
                    " (Reset operation failed: " + e.getMessage() + ")");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行重置操作失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 发送请求到浏览器操作服务
     * (Send request to browser operation service)
     */
    private Map<String, Object> sendRequest(Map<String, Object> requestBody) {
        try {
            String requestUrl = browserServiceConfig.getUrl() + "/api/execute";
            log.debug("发送请求到浏览器操作服务: {} (Sending request to browser operation service: {})", requestUrl, requestUrl);
            
            // 打印请求参数 (Print request parameters)
            log.debug("请求参数: {} (Request parameters: {})", requestBody, requestBody);
            
            // 构建符合Python服务要求的请求结构
            Map<String, Object> apiRequestBody = new HashMap<>();
            apiRequestBody.put("operation", requestBody.get("action"));
            apiRequestBody.put("parameters", requestBody.get("parameters"));
            
            // 将对象序列化为JSON字符串 (Serialize object to JSON string)
            String requestBodyStr = objectMapper.writeValueAsString(apiRequestBody);
            
            // 设置请求头 (Set request headers)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体 (Create request entity)
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyStr, headers);
            
            // 发送请求并获取响应 (Send request and get response)
            String responseBody = restTemplate.postForObject(requestUrl, requestEntity, String.class);
            
            // 解析响应 (Parse response)
            Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            
            // 检查响应是否包含error字段，但实际操作可能已执行
            if (response.containsKey("error")) {
                log.warn("操作返回错误信息，但可能已执行: {} (Operation returned error, but may have executed: {})", 
                        response.get("error"), response.get("error"));
            }
            
            // 确保响应中包含必要的字段
            if (!response.containsKey("success")) {
                response.put("success", false);
                response.put("message", "服务未返回success字段");
            }
            
            return response;
        } catch (ResourceAccessException e) {
            // 处理连接异常，可能是服务未运行
            log.error("无法连接到浏览器操作服务: {} (Cannot connect to browser operation service: {})", e.getMessage(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无法连接到浏览器服务: " + e.getMessage() + "，请确保服务已启动");
            return errorResponse;
        } catch (RestClientException e) {
            // 处理REST客户端异常
            log.error("REST客户端异常: {} (REST client exception: {})", e.getMessage(), e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "REST客户端异常: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            log.error("发送请求到浏览器操作服务时出错 (Error sending request to browser operation service)", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "请求浏览器服务失败: " + e.getMessage() + " (Failed to request browser service)");
            return errorResponse;
        }
    }
} 