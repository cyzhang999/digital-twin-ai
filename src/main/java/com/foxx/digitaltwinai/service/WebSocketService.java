package com.foxx.digitaltwinai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 * (WebSocket Service)
 */
public interface WebSocketService {
    
    /**
     * 发送日志消息
     * (Send Log Message)
     *
     * @param message 消息内容 (Message Content)
     */
    void sendLog(String message);
    
    /**
     * 发送错误消息
     * (Send Error Message)
     *
     * @param message 错误消息 (Error Message)
     */
    void sendError(String message);
    
    /**
     * 发送状态消息
     * (Send Status Message)
     *
     * @param message 状态消息 (Status Message)
     */
    void sendStatus(String message);
    
    /**
     * 发送操作结果
     * (Send Operation Result)
     *
     * @param success 是否成功 (Success Flag)
     * @param message 结果消息 (Result Message)
     * @param data 数据 (Data)
     */
    void sendOperationResult(boolean success, String message, Object data);
}

/**
 * WebSocket服务实现
 * (WebSocket Service Implementation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
class WebSocketServiceImpl implements WebSocketService {
    
    /**
     * WebSocket消息模板
     * (WebSocket Messaging Template)
     */
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * JSON对象映射器
     * (JSON Object Mapper)
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 日志目标
     * (Log Destination)
     */
    private static final String LOG_DESTINATION = "/topic/logs";
    
    /**
     * 错误目标
     * (Error Destination)
     */
    private static final String ERROR_DESTINATION = "/topic/errors";
    
    /**
     * 状态目标
     * (Status Destination)
     */
    private static final String STATUS_DESTINATION = "/topic/status";
    
    /**
     * 操作结果目标
     * (Operation Result Destination)
     */
    private static final String OPERATION_RESULT_DESTINATION = "/topic/operationResults";
    
    /**
     * 发送日志消息
     * (Send Log Message)
     *
     * @param message 消息内容 (Message Content)
     */
    @Override
    public void sendLog(String message) {
        try {
            log.debug("发送WebSocket日志消息: {} (Sending WebSocket log message: {})", message, message);
            messagingTemplate.convertAndSend(LOG_DESTINATION, buildMessage("log", message));
        } catch (Exception e) {
            log.error("发送WebSocket日志消息失败 (Failed to send WebSocket log message)", e);
        }
    }
    
    /**
     * 发送错误消息
     * (Send Error Message)
     *
     * @param message 错误消息 (Error Message)
     */
    @Override
    public void sendError(String message) {
        try {
            log.debug("发送WebSocket错误消息: {} (Sending WebSocket error message: {})", message, message);
            messagingTemplate.convertAndSend(ERROR_DESTINATION, buildMessage("error", message));
        } catch (Exception e) {
            log.error("发送WebSocket错误消息失败 (Failed to send WebSocket error message)", e);
        }
    }
    
    /**
     * 发送状态消息
     * (Send Status Message)
     *
     * @param message 状态消息 (Status Message)
     */
    @Override
    public void sendStatus(String message) {
        try {
            log.debug("发送WebSocket状态消息: {} (Sending WebSocket status message: {})", message, message);
            messagingTemplate.convertAndSend(STATUS_DESTINATION, buildMessage("status", message));
        } catch (Exception e) {
            log.error("发送WebSocket状态消息失败 (Failed to send WebSocket status message)", e);
        }
    }
    
    /**
     * 发送操作结果
     * (Send Operation Result)
     *
     * @param success 是否成功 (Success Flag)
     * @param message 结果消息 (Result Message)
     * @param data 数据 (Data)
     */
    @Override
    public void sendOperationResult(boolean success, String message, Object data) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", message);
            if (data != null) {
                result.put("data", data);
            }
            
            log.debug("发送WebSocket操作结果: {} (Sending WebSocket operation result: {})", message, message);
            messagingTemplate.convertAndSend(OPERATION_RESULT_DESTINATION, result);
        } catch (Exception e) {
            log.error("发送WebSocket操作结果失败 (Failed to send WebSocket operation result)", e);
        }
    }
    
    /**
     * 构建消息
     * (Build Message)
     *
     * @param type 消息类型 (Message Type)
     * @param content 消息内容 (Message Content)
     * @return 消息映射 (Message Map)
     */
    private Map<String, Object> buildMessage(String type, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }
} 