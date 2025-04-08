package com.foxx.digitaltwinai.service;

import com.foxx.digitaltwinai.model.ChatRequest;
import com.foxx.digitaltwinai.model.ChatResponse;

/**
 * Dify服务接口
 * (Dify Service Interface)
 */
public interface DifyService {
    
    /**
     * 发送聊天消息
     * (Send Chat Message)
     * 
     * @param request 聊天请求 (Chat Request)
     * @return 聊天响应 (Chat Response)
     */
    ChatResponse sendChatMessage(ChatRequest request);
} 