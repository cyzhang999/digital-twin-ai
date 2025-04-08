package com.foxx.digitaltwinai.controller;

import com.foxx.digitaltwinai.model.ChatRequest;
import com.foxx.digitaltwinai.model.ChatResponse;
import com.foxx.digitaltwinai.service.BrowserService;
import com.foxx.digitaltwinai.service.DifyService;
import com.foxx.digitaltwinai.service.WebSocketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天控制器
 * (Chat Controller)
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {
    
    /**
     * Dify服务
     * (Dify Service)
     */
    private final DifyService difyService;
    
    /**
     * 浏览器服务
     * (Browser Service)
     */
    private final BrowserService browserService;
    
    /**
     * WebSocket服务
     * (WebSocket Service)
     */
    private final WebSocketService webSocketService;
    
    /**
     * 处理聊天请求
     * (Handle Chat Request)
     *
     * @param request 聊天请求 (Chat Request)
     * @return 聊天响应 (Chat Response)
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody(required = false) ChatRequest request) {
        try {
            // 检查请求是否为null
            if (request == null) {
                log.warn("收到null请求 (Received null request)");
                ChatResponse errorResponse = ChatResponse.builder()
                    .text("很抱歉，接收到空请求。请确保发送了有效的消息内容。")
                    .sessionId(UUID.randomUUID().toString())
                    .build();
                
                // 添加默认的失败结果
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "请求内容为空");
                errorResponse.setResult(errorResult);
                
                return ResponseEntity.ok(errorResponse);
            }

            // 检查消息是否为null或空
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.warn("收到空消息内容 (Received empty message content)");
                ChatResponse errorResponse = ChatResponse.builder()
                    .text("很抱歉，我无法理解空消息。请提供具体的指令或问题。")
                    .sessionId(UUID.randomUUID().toString())
                    .build();
                
                // 添加默认的失败结果
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "消息内容为空");
                errorResponse.setResult(errorResult);
                
                return ResponseEntity.ok(errorResponse);
            }

            log.info("收到聊天请求: {} (Received chat request: {})", request.getMessage(), request.getMessage());
            
            // 调用Dify服务发送聊天消息
            // (Call Dify service to send chat message)
            ChatResponse response = difyService.sendChatMessage(request);
            
            // 如果没有会话ID，创建一个
            // (If no session ID, create one)
            if (response.getSessionId() == null) {
                response.setSessionId(UUID.randomUUID().toString());
            }
            
            // 如果没有文本内容但有回答，将回答设置为文本内容
            // (If no text content but has answer, set answer as text content)
            if ((response.getText() == null || response.getText().isEmpty()) && response.getAnswer() != null) {
                response.setText(response.getAnswer());
            }
            
            // 如果没有结果字段，添加默认的成功结果
            // (If no result field, add default success result)
            if (response.getResult() == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "操作完成");
                response.setResult(result);
            }
            
            log.info("返回聊天响应: {} (Returning chat response: {})", 
                    response.getText() != null ? response.getText().substring(0, Math.min(response.getText().length(), 50)) + "..." : "null", 
                    response.getText() != null ? response.getText().substring(0, Math.min(response.getText().length(), 50)) + "..." : "null");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理聊天请求时出错 (Error processing chat request)", e);
            
            // 构建错误响应
            // (Build error response)
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "处理请求时出错: " + e.getMessage());
            
            ChatResponse errorResponse = ChatResponse.builder()
                    .text("很抱歉，处理您的请求时出现了错误: " + e.getMessage())
                    .sessionId(UUID.randomUUID().toString())
                    .result(errorResult)
                    .build();
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 处理模型操作请求
     * @param action 模型操作
     * @return 操作结果
     */
    private Map<String, Object> handleModelAction(Map<String, Object> action) {
        if (action == null) {
            return Map.of(
                "success", false,
                "message", "无效的操作 (Invalid action)"
            );
        }

        try {
            // 获取操作类型、目标和参数
            String type = (String) action.get("type");
            String target = (String) action.get("target");
            Map<String, Object> params = (Map<String, Object>) action.getOrDefault("params", Map.of());

            // 记录操作请求
            log.info("执行模型操作: type={}, target={}, params={}", type, target, params);

            // 使用browserService执行相应操作
            switch (type.toLowerCase()) {
                case "rotate":
                    return browserService.executeRotate(params);
                case "zoom":
                    return browserService.executeZoom(params);
                case "focus":
                    return browserService.executeFocus(target);
                case "reset":
                    return browserService.executeReset();
                default:
                    return Map.of(
                        "success", false,
                        "message", "不支持的操作类型: " + type + " (Unsupported action type)"
                    );
            }
        } catch (Exception e) {
            log.error("执行模型操作失败: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "操作执行异常: " + e.getMessage() + " (Operation execution exception)"
            );
        }
    }
} 