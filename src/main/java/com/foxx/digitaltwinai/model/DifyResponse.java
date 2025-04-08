package com.foxx.digitaltwinai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Dify API响应模型
 * (Dify API Response Model)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyResponse {
    
    /**
     * 响应ID
     * (Response ID)
     */
    private String id;
    
    /**
     * 对话ID
     * (Conversation ID)
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 创建时间
     * (Created At)
     */
    @JsonProperty("created_at")
    private long createdAt;
    
    /**
     * 响应文本
     * (Response Text)
     */
    private String answer;
    
    /**
     * 操作指令
     * (Action Command)
     */
    private Map<String, Object> action;
    
    /**
     * 事件类型
     * (Event Type)
     * 流式响应中的事件类型，如"message"或"error"
     * (Event type in streaming response, such as "message" or "error")
     */
    private String event;
    
    /**
     * 任务ID
     * (Task ID)
     */
    @JsonProperty("task_id")
    private String taskId;
    
    /**
     * 消息ID
     * (Message ID)
     */
    @JsonProperty("message_id")
    private String messageId;
    
    /**
     * 变量选择器
     * (Variable Selector)
     */
    @JsonProperty("from_variable_selector")
    private Object fromVariableSelector;
    
    /**
     * 引用消息
     * (Referenced Messages)
     */
    @JsonProperty("retriever_resources")
    private List<RetrieverResource> retrieverResources;
    
    /**
     * 引用资源
     * (Reference Resource)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrieverResource {
        
        /**
         * 文档ID
         * (Document ID)
         */
        @JsonProperty("document_id")
        private String documentId;
        
        /**
         * 文档名称
         * (Document Name)
         */
        @JsonProperty("document_name")
        private String documentName;
        
        /**
         * 相似度分数
         * (Similarity Score)
         */
        private double score;
        
        /**
         * 引用内容
         * (Reference Content)
         */
        private String content;
    }
} 