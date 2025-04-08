package com.foxx.digitaltwinai.controller;

import com.foxx.digitaltwinai.model.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 * (WebSocket Controller)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    /**
     * 发送日志消息
     * (Send Log Message)
     * 
     * @param message 消息 (Message)
     * @return 消息 (Message)
     */
    @MessageMapping("/logs")
    @SendTo("/topic/logs")
    public WebSocketMessage sendLog(WebSocketMessage message) {
        return message;
    }
    
    /**
     * 发送错误消息
     * (Send Error Message)
     * 
     * @param message 消息 (Message)
     * @return 消息 (Message)
     */
    @MessageMapping("/errors")
    @SendTo("/topic/errors")
    public WebSocketMessage sendError(WebSocketMessage message) {
        return message;
    }
    
    /**
     * 发送状态消息
     * (Send Status Message)
     * 
     * @param message 消息 (Message)
     * @return 消息 (Message)
     */
    @MessageMapping("/status")
    @SendTo("/topic/status")
    public WebSocketMessage sendStatus(WebSocketMessage message) {
        return message;
    }
} 