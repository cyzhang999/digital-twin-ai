package com.foxx.digitaltwinai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket消息模型
 * (WebSocket Message Model)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    /**
     * 消息类型：LOG, ERROR, STATUS
     * (Message Type: LOG, ERROR, STATUS)
     */
    private MessageType type;
    
    /**
     * 消息内容
     * (Message Content)
     */
    private String content;
    
    /**
     * 时间戳
     * (Timestamp)
     */
    private LocalDateTime timestamp;
    
    /**
     * 消息类型枚举
     * (Message Type Enum)
     */
    public enum MessageType {
        /**
         * 日志消息
         * (Log Message)
         */
        LOG,
        
        /**
         * 错误消息
         * (Error Message)
         */
        ERROR,
        
        /**
         * 状态消息
         * (Status Message)
         */
        STATUS
    }
    
    /**
     * 创建日志消息
     * (Create Log Message)
     */
    public static WebSocketMessage createLog(String content) {
        return WebSocketMessage.builder()
                .type(MessageType.LOG)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建错误消息
     * (Create Error Message)
     */
    public static WebSocketMessage createError(String content) {
        return WebSocketMessage.builder()
                .type(MessageType.ERROR)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建状态消息
     * (Create Status Message)
     */
    public static WebSocketMessage createStatus(String content) {
        return WebSocketMessage.builder()
                .type(MessageType.STATUS)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 