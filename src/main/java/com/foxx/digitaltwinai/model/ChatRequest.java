package com.foxx.digitaltwinai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 聊天请求模型
 * (Chat Request Model)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * 用户消息内容
     * (User Message Content)
     */
    @NotBlank(message = "消息内容不能为空 (Message content cannot be empty)")
    private String message;
    
    /**
     * 会话ID，用于保持对话上下文
     * (Session ID for Maintaining Conversation Context)
     */
    private String sessionId;
    
    /**
     * 附加数据
     * (Additional Metadata)
     */
    private Map<String, Object> metadata;
} 