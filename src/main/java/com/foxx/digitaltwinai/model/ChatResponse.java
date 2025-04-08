package com.foxx.digitaltwinai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 聊天响应模型
 * (Chat Response Model)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * 响应文本
     * (Response Text)
     */
    private String text;
    
    /**
     * 回答内容
     * (Answer Content)
     */
    private String answer;
    
    /**
     * 操作指令
     * (Action Command)
     */
    @JsonProperty("action")
    private Map<String, Object> action;
    
    /**
     * 操作结果
     * (Operation Result)
     */
    private Map<String, Object> result;
    
    /**
     * 会话ID
     * (Session ID)
     */
    @JsonProperty("sessionId")
    private String sessionId;
    
    /**
     * 操作指令模型
     * (Action Command Model)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionCommand {
        
        /**
         * 操作类型：rotate, zoom, focus, reset
         * (Operation Type: rotate, zoom, focus, reset)
         */
        private String type;
        
        /**
         * 目标部件名称
         * (Target Component Name)
         */
        private String target;
        
        /**
         * 操作参数
         * (Operation Parameters)
         */
        private Map<String, Object> params;
    }
} 