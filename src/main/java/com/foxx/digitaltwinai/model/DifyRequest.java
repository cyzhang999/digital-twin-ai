package com.foxx.digitaltwinai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Dify API请求模型
 * (Dify API Request Model)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyRequest {
    
    /**
     * 对话输入信息
     * (Conversation Inputs)
     */
    private Map<String, String> inputs;
    
    /**
     * 查询内容
     * (Query)
     */
    private String query;
    
    /**
     * 对话历史消息
     * (Conversation History)
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 响应模式：blocking或streaming
     * (Response Mode: blocking or streaming)
     */
    @JsonProperty("response_mode")
    private String responseMode;
    
    /**
     * 用户标识
     * (User Identifier)
     */
    private String user;
    
    /**
     * 消息列表
     * (Message List)
     */
    private List<Message> messages;
    
    /**
     * 消息模型
     * (Message Model)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        
        /**
         * 消息内容
         * (Message Content)
         */
        private String content;
        
        /**
         * 消息角色：user, assistant
         * (Message Role: user, assistant)
         */
        private String role;
    }
}