package com.foxx.digitaltwinai.service.impl;

import com.foxx.digitaltwinai.config.DifyApiConfig;
import com.foxx.digitaltwinai.model.ChatRequest;
import com.foxx.digitaltwinai.model.ChatResponse;
import com.foxx.digitaltwinai.model.DifyRequest;
import com.foxx.digitaltwinai.model.DifyResponse;
import com.foxx.digitaltwinai.service.BrowserService;
import com.foxx.digitaltwinai.service.DifyRequestLogService;
import com.foxx.digitaltwinai.service.DifyService;
import com.foxx.digitaltwinai.util.HmacUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dify服务实现类
 * (Dify Service Implementation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {
    
    /**
     * Dify API配置
     * (Dify API Configuration)
     */
    private final DifyApiConfig difyApiConfig;
    
    /**
     * RestTemplate
     */
    private final RestTemplate restTemplate;
    
    /**
     * HMAC工具
     * (HMAC Utility)
     */
    private final HmacUtils hmacUtils;
    
    /**
     * Dify请求日志服务
     * (Dify Request Log Service)
     */
    private final DifyRequestLogService difyRequestLogService;
    
    /**
     * 浏览器服务
     * (Browser Service)
     */
    private final BrowserService browserService;
    
    /**
     * JSON对象映射器
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 发送聊天消息
     * (Send Chat Message)
     *
     * @param request 聊天请求 (Chat Request)
     * @return 聊天响应 (Chat Response)
     */
    @Override
    public ChatResponse sendChatMessage(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String requestJson = null;
        String responseJson = null;
        Map<String, Object> actionMap = null;
        String operationType = null;
        String targetComponent = null;
        
        // 请求验证 - 防止空消息导致空指针异常
        if (request == null) {
            log.warn("收到null请求，返回友好提示");
            return ChatResponse.builder()
                    .text("很抱歉，接收到空请求。请确保发送了有效的消息内容。")
                    .build();
        }
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            log.warn("收到空请求或空消息，返回友好提示");
            return ChatResponse.builder()
                    .text("很抱歉，我无法理解空消息。请提供具体的指令或问题。")
                    .build();
        }
        
        String userMessage = request.getMessage().trim();
        
        // 尝试使用本地回退逻辑解析指令
        boolean shouldFallback = false;
        
        // 先尝试本地解析指令
        if (difyApiConfig.getFallback().isEnabled()) {
            // 尝试本地解析指令
            ChatResponse.ActionCommand actionCommand = parseInstruction(userMessage);
            if (actionCommand != null) {
                // 转换为Map用于后续操作
                actionMap = convertActionCommandToMap(actionCommand);
                
                operationType = actionCommand.getType();
                targetComponent = actionCommand.getTarget();
                
                log.info("本地解析指令成功: {} -> 类型={}, 目标={} (Local instruction parsing successful)",
                        userMessage, operationType, targetComponent);
                
                // 直接执行模型操作
                try {
                    Map<String, Object> operationResult = browserService.executeModelOperation(actionMap);
                    boolean success = (boolean) operationResult.getOrDefault("success", false);
                    
                    if (success) {
                        // 生成本地回复文本
                        String localResponse = generateLocalResponse(actionCommand);
                        
                        // 记录成功请求（使用本地解析）
                        try {
                            difyRequestLogService.recordRequest(
                                    userMessage,  // 确保用户指令不为空
                                    "本地解析指令: " + operationType,
                                    "直接执行操作: " + localResponse,
                                    System.currentTimeMillis() - startTime,
                                    1, // 成功状态 (Success status)
                                    null,
                                    operationType,
                                    targetComponent
                            );
                        } catch (Exception e) {
                            log.error("记录请求日志失败", e);
                            // 继续处理，不让日志错误影响主流程
                        }
                        
                        // 构建本地解析响应
                        return ChatResponse.builder()
                                .text(localResponse)
                                .action(actionMap)
                                .result(operationResult)
                                .sessionId(UUID.randomUUID().toString())
                                .build();
                    } else {
                        // 操作执行失败，继续尝试调用Dify API
                        log.warn("本地解析的指令执行失败，继续调用Dify API (Local instruction execution failed, continuing with Dify API)");
                        shouldFallback = false; // 重置fallback标志，因为我们将尝试调用API
                    }
                } catch (Exception e) {
                    log.warn("执行本地解析的指令时出错，继续调用Dify API (Error executing locally parsed instruction, continuing with Dify API)", e);
                    shouldFallback = false; // 重置fallback标志，因为我们将尝试调用API
                }
            }
        }
        
        try {
            log.debug("发送Dify请求: {} (Sending Dify request: {})", userMessage, userMessage);
            
            // 检查服务名称配置
            if (difyApiConfig.getServiceName() == null || difyApiConfig.getServiceName().isEmpty()) {
                log.warn("Dify服务名称(company_name)未配置，使用默认值: digital-twin (Dify service name not configured, using default value)");
                difyApiConfig.setServiceName("digital-twin");
            }
            
            // 构建Dify请求 (Build Dify request)
            DifyRequest difyRequest = buildDifyRequest(request);
            
            try {
                requestJson = objectMapper.writeValueAsString(difyRequest);
            } catch (JsonProcessingException e) {
                log.warn("请求序列化失败: {} (Request serialization failed: {})", e.getMessage(), e.getMessage());
                requestJson = "序列化失败: " + e.getMessage();
            }
            
            // 构建HTTP头 (Build HTTP headers)
            HttpHeaders headers = buildHeaders();
            headers.set("Authorization", "Bearer app-9v8uNvGpo576ojUarkP8kZNT");
            
            // 发送请求 (Send request)
            HttpEntity<DifyRequest> entity = new HttpEntity<>(difyRequest, headers);
            
            try {
                DifyResponse response = null;
                
                // 检查响应模式 (Check response mode)
                if ("streaming".equals(difyRequest.getResponseMode())) {
                    log.debug("使用流式响应模式 (Using streaming response mode)");
                    
                    // 对于流式响应，我们需要获取所有消息块并合并结果
                    // (For streaming responses, we need to get all message blocks and merge the results)
                    String responseBody = restTemplate.postForObject(
                            difyApiConfig.getChatMessagesUrl(),
                            entity,
                            String.class
                    );
                    
                    if (responseBody != null && !responseBody.isEmpty()) {
                        log.debug("收到流式响应，长度为 (Received streaming response with length): {}", responseBody.length());
                        
                        // 从流式响应中提取所有有效的回答内容
                        // (Extract all valid answer content from the streaming response)
                        String combinedAnswer = extractCombinedAnswerFromStream(responseBody);
                        
                        // 提取最后一个有效消息用于获取会话ID等元数据
                        // (Extract the last valid message to get conversation ID and other metadata)
                        DifyResponse lastMessage = extractLastValidMessage(responseBody);
                        
                        if (lastMessage != null) {
                            // 使用合并的回答替换最后一条消息的回答内容
                            // (Replace the answer content of the last message with the combined answer)
                            lastMessage.setAnswer(combinedAnswer);
                            response = lastMessage;
                            log.debug("成功提取并合并流式响应内容 (Successfully extracted and combined streaming response content)");
                        } else {
                            // 如果无法提取有效消息，创建一个基本响应
                            // (If unable to extract valid message, create a basic response)
                            response = DifyResponse.builder()
                                    .answer(combinedAnswer != null ? combinedAnswer : "接收到AI响应，但内容可能不完整")
                                    .build();
                        }
                    }
                    
                    // 如果未能从流式响应中提取消息，创建一个基本响应
                    // (If failed to extract message from streaming response, create a basic response)
                    if (response == null) {
                        response = DifyResponse.builder()
                                .answer("接收到AI响应，但无法解析具体内容")
                                .build();
                    }
                } else {
                    // 对于非流式响应，直接使用常规方式解析
                    // (For non-streaming responses, parse in the regular way)
                    response = restTemplate.postForObject(
                    difyApiConfig.getChatMessagesUrl(),
                    entity,
                    DifyResponse.class
            );
                }
            
            if (response == null) {
                throw new RuntimeException("Dify API响应为空 (Dify API response is null)");
            }
                
                try {
                    responseJson = objectMapper.writeValueAsString(response);
                } catch (JsonProcessingException e) {
                    log.warn("响应序列化失败: {} (Response serialization failed: {})", e.getMessage(), e.getMessage());
                    responseJson = "序列化失败: " + e.getMessage();
                }
            
            log.debug("Dify响应: {} (Dify response: {})", response.getAnswer(), response.getAnswer());
            
            // 解析操作指令 (Parse action command)
            ChatResponse.ActionCommand actionCommand = parseAction(response.getAction());
                
                if (actionCommand != null) {
                    // 转换为Map用于后续操作
                    actionMap = convertActionCommandToMap(actionCommand);
                    
                    operationType = actionCommand.getType();
                    targetComponent = actionCommand.getTarget();
                    
                    // 直接执行模型操作
                    try {
                        Map<String, Object> operationResult = browserService.executeModelOperation(actionMap);
                        boolean success = (boolean) operationResult.getOrDefault("success", false);
                        
                        if (!success) {
                            String errorMessage = (String) operationResult.getOrDefault("error", "未知错误");
                            log.warn("执行Dify返回的操作指令失败: {} (Failed to execute Dify operation: {})", errorMessage, errorMessage);
                        }
                        
                        // 为JSON响应添加操作结果
                        Map<String, Object> resultMap = new HashMap<>();
                        if (operationResult.containsKey("message")) {
                            resultMap.put("message", operationResult.get("message"));
                            response.setAnswer((String)operationResult.get("message"));
                        }
                        resultMap.put("success", success);
                        resultMap.put("operation", operationType);
                        
                        // 如果是旋转操作，获取角度和方向信息
                        if ("rotate".equals(operationType) && actionCommand.getParams() != null) {
                            Map<String, Object> params = actionCommand.getParams();
                            if (params.containsKey("angle") && params.containsKey("direction")) {
                                resultMap.put("angle", params.get("angle"));
                                resultMap.put("direction", params.get("direction"));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("执行Dify操作指令时出错 (Error executing Dify operation)", e);
                    }
                }
                
            } catch (ResourceAccessException e) {
                log.warn("Dify API连接失败，将使用本地回退逻辑 (Dify API connection failed, will use local fallback logic)", e);
                shouldFallback = true;
                // 继续执行，让本地回退逻辑处理
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("company_name is required")) {
                    log.error("Dify API调用失败: 缺少company_name参数 (Failed to call Dify API: company_name parameter is required)", e);
                    throw new RuntimeException("Dify API配置错误: 缺少company_name参数，请检查service-name配置 (Dify API configuration error: company_name parameter is required, please check service-name configuration)", e);
                } else if (e.getMessage() != null && e.getMessage().contains("equipment_type is required")) {
                    log.error("Dify API调用失败: 缺少equipment_type参数 (Failed to call Dify API: equipment_type parameter is required)", e);
                    throw new RuntimeException("Dify API配置错误: 缺少equipment_type参数 (Dify API configuration error: equipment_type parameter is required)", e);
                } else {
                    log.warn("Dify API调用失败，将使用本地回退逻辑 (Dify API call failed, will use local fallback logic)", e);
                    shouldFallback = true;
                    // 继续执行，让本地回退逻辑处理
                }
            }
            
            // 若Dify API调用失败，启用本地回退逻辑
            if (shouldFallback && difyApiConfig.getFallback().isAutoRetryOnFailure()) {
                ChatResponse.ActionCommand actionCommand = parseInstruction(userMessage);
                if (actionCommand != null) {
                    // 转换为Map用于后续操作
                    actionMap = convertActionCommandToMap(actionCommand);
                    
                    operationType = actionCommand.getType();
                    targetComponent = actionCommand.getTarget();
                    
                    // 直接执行模型操作
                    try {
                        Map<String, Object> operationResult = browserService.executeModelOperation(actionMap);
                        boolean success = (boolean) operationResult.getOrDefault("success", false);
                        
                        if (success) {
                            // 生成本地回复文本
                            String localResponse = generateLocalResponse(actionCommand);
                            
                            // 记录使用了本地回退逻辑
                            log.info("使用本地回退逻辑成功解析并执行指令: {} -> 类型={}, 目标={} (Successfully parsed and executed instruction with local fallback logic)",
                                    userMessage, operationType, targetComponent);
                            
                            // 记录成功请求（使用本地回退）
                            try {
                                difyRequestLogService.recordRequest(
                                        userMessage,
                                        requestJson != null ? requestJson : "无请求数据",
                                        "使用本地回退逻辑: " + localResponse,
                                        System.currentTimeMillis() - startTime,
                                        1, // 成功状态 (Success status)
                                        null,
                                        operationType,
                                        targetComponent
                                );
                            } catch (Exception e) {
                                log.error("记录请求日志失败", e);
                                // 继续处理，不让日志错误影响主流程
                            }
                            
                            // 构建本地回退响应
                            return ChatResponse.builder()
                                    .text(localResponse)
                                    .action(actionMap)
                                    .sessionId(UUID.randomUUID().toString())
                                    .build();
                        } else {
                            String errorMessage = (String) operationResult.getOrDefault("error", "未知错误");
                            log.warn("执行本地回退的操作指令失败: {} (Failed to execute local fallback operation: {})", errorMessage, errorMessage);
                        }
                    } catch (Exception e) {
                        log.warn("执行本地回退指令时出错 (Error executing local fallback instruction)", e);
                    }
                }
            }
            
            // 记录成功请求 (Record successful request)
            try {
                difyRequestLogService.recordRequest(
                        userMessage,
                        requestJson != null ? requestJson : "无请求数据",
                        responseJson != null ? responseJson : "无响应数据",
                        System.currentTimeMillis() - startTime,
                        1, // 成功状态 (Success status)
                        null,
                        operationType,
                        targetComponent
                );
            } catch (Exception e) {
                log.error("记录请求日志失败", e);
                // 继续处理，不让日志错误影响主流程
            }
            
            // 构建响应 (Build response)
            String finalText = "操作已执行";
            if (responseJson != null && responseJson.length() > 0) {
                try {
                    DifyResponse difyResponse = objectMapper.readValue(responseJson, DifyResponse.class);
                    if (difyResponse != null && difyResponse.getAnswer() != null) {
                        finalText = difyResponse.getAnswer();
                    }
                } catch (Exception e) {
                    log.warn("解析响应JSON失败，使用原始文本", e);
                    finalText = responseJson;
                }
            }
            
            return ChatResponse.builder()
                    .text(finalText)
                    .action(actionMap)
                    .sessionId(actionMap != null ? UUID.randomUUID().toString() : null)
                    .build();
            
        } catch (Exception e) {
            if (e instanceof RuntimeException && e.getMessage() != null && 
                (e.getMessage().contains("company_name") || e.getMessage().contains("equipment_type"))) {
                // 重新抛出配置错误
                throw e;
            }
            
            log.warn("Dify服务调用出错，尝试使用本地回退逻辑 (Dify service call error, trying local fallback logic)", e);
            
            // 尝试本地解析指令
            ChatResponse.ActionCommand actionCommand = parseInstruction(userMessage);
            if (actionCommand != null) {
                // 转换为Map用于后续操作
                actionMap = convertActionCommandToMap(actionCommand);
                
                operationType = actionCommand.getType();
                targetComponent = actionCommand.getTarget();
                
                // 直接执行模型操作
                try {
                    Map<String, Object> operationResult = browserService.executeModelOperation(actionMap);
                    boolean success = (boolean) operationResult.getOrDefault("success", false);
                    
                    if (success) {
                        // 生成本地回复文本
                        String localResponse = generateLocalResponse(actionCommand);
                        
                        log.info("使用本地回退逻辑成功解析并执行指令: {} -> 类型={}, 目标={} (Successfully parsed and executed instruction with local fallback logic)",
                                userMessage, operationType, targetComponent);
                        
                        // 记录成功请求（使用本地回退）
                        try {
                            difyRequestLogService.recordRequest(
                                    userMessage,
                                    requestJson != null ? requestJson : "无请求数据",
                                    "使用本地回退逻辑: " + localResponse,
                                    System.currentTimeMillis() - startTime,
                                    1, // 成功状态 (Success status)
                                    null,
                                    operationType,
                                    targetComponent
                            );
                        } catch (Exception ex) {
                            log.error("记录请求日志失败", ex);
                            // 继续处理，不让日志错误影响主流程
                        }
                        
                        // 构建本地回退响应
                        return ChatResponse.builder()
                                .text(localResponse)
                                .action(actionMap)
                                .sessionId(UUID.randomUUID().toString())
                                .build();
                    } else {
                        String errorMessage = (String) operationResult.getOrDefault("error", "未知错误");
                        log.warn("执行本地回退的操作指令失败: {} (Failed to execute local fallback operation: {})", errorMessage, errorMessage);
                    }
                } catch (Exception ex) {
                    log.warn("执行本地回退指令时出错 (Error executing local fallback instruction)", ex);
                }
            }
            
            // 如果本地解析也失败，则继续抛出异常
            throw e;
        }
    }
    
    /**
     * 本地解析用户指令
     * (Parse user instruction locally)
     * 
     * @param instruction 用户指令 (User instruction)
     * @return 操作指令 (Action command)
     */
    private ChatResponse.ActionCommand parseInstruction(String instruction) {
        // 防空检查
        if (instruction == null || instruction.isEmpty()) {
            log.warn("用户指令为空");
            return null;
        }
        
        instruction = instruction.toLowerCase().trim();
        
        // 重置视图
        if (instruction.contains("重置") || instruction.contains("reset")) {
            return ChatResponse.ActionCommand.builder()
                    .type("reset")
                    .build();
        }
        
        // 聚焦操作
        Pattern focusPattern = Pattern.compile("聚焦(?:到|)?(\\w+[\\d_]*)", Pattern.CASE_INSENSITIVE);
        Matcher focusMatcher = focusPattern.matcher(instruction);
        if (focusMatcher.find()) {
            String target = focusMatcher.group(1);
            return ChatResponse.ActionCommand.builder()
                    .type("focus")
                    .target(target)
                    .build();
        }
        
        // 旋转操作
        Pattern rotatePattern = Pattern.compile("(?:向|)(左|右)(?:旋转|)(?:(\\d+)(?:度|))?", Pattern.CASE_INSENSITIVE);
        Matcher rotateMatcher = rotatePattern.matcher(instruction);
        if (rotateMatcher.find() || instruction.contains("旋转")) {
            String direction = "left"; // 默认方向
            int angle = 30; // 默认角度
            
            if (rotateMatcher.find()) {
                direction = rotateMatcher.group(1).equals("右") ? "right" : "left";
                if (rotateMatcher.group(2) != null) {
                    try {
                        angle = Integer.parseInt(rotateMatcher.group(2));
                    } catch (NumberFormatException e) {
                        // 使用默认角度
                    }
                }
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("direction", direction);
            params.put("angle", angle);
            
            return ChatResponse.ActionCommand.builder()
                    .type("rotate")
                    .params(params)
                    .build();
        }
        
        // 缩放操作
        Pattern zoomPattern = Pattern.compile("(?:放大|缩小)(\\d+(?:\\.\\d+)?)(?:倍|)", Pattern.CASE_INSENSITIVE);
        Matcher zoomMatcher = zoomPattern.matcher(instruction);
        if (zoomMatcher.find() || instruction.contains("放大") || instruction.contains("缩小")) {
            float scale = 1.5f; // 默认比例
            
            if (zoomMatcher.find()) {
                try {
                    scale = Float.parseFloat(zoomMatcher.group(1));
                } catch (NumberFormatException e) {
                    // 使用默认比例
                }
            }
            
            // 如果是缩小，将比例设为小于1
            if (instruction.contains("缩小") && scale > 1) {
                scale = 1 / scale;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("scale", scale);
            
            return ChatResponse.ActionCommand.builder()
                    .type("zoom")
                    .params(params)
                    .build();
        }
        
        // 没有匹配的指令
        return null;
    }
    
    /**
     * 为本地处理的指令生成响应文本
     * (Generate response text for locally processed instructions)
     * 
     * @param actionCommand 操作指令 (Action command)
     * @return 响应文本 (Response text)
     */
    private String generateLocalResponse(ChatResponse.ActionCommand actionCommand) {
        if (actionCommand == null) {
            return "很抱歉，我无法理解您的指令。";
        }
        
        switch (actionCommand.getType()) {
            case "reset":
                return "已为您重置视图，恢复到默认状态。";
            case "focus":
                return "已聚焦到" + actionCommand.getTarget() + "区域，您可以近距离查看该区域的细节。";
            case "rotate":
                Map<String, Object> rotateParams = actionCommand.getParams();
                String direction = (String) rotateParams.getOrDefault("direction", "left");
                Object angle = rotateParams.getOrDefault("angle", 30);
                return "已将视图向" + (direction.equals("left") ? "左" : "右") + "旋转" + angle + "度。";
            case "zoom":
                Map<String, Object> zoomParams = actionCommand.getParams();
                Object scale = zoomParams.getOrDefault("scale", 1.5);
                float scaleValue = 0;
                if (scale instanceof Integer) {
                    scaleValue = ((Integer) scale).floatValue();
                } else if (scale instanceof Float) {
                    scaleValue = (Float) scale;
                } else if (scale instanceof Double) {
                    scaleValue = ((Double) scale).floatValue();
                }
                
                if (scaleValue > 1) {
                    return "已将视图放大" + scaleValue + "倍。";
                } else {
                    return "已将视图缩小至原来的" + scaleValue + "倍。";
                }
            default:
                return "操作已执行。";
        }
    }
    
    /**
     * 构建Dify请求
     * (Build Dify Request)
     *
     * @param request 聊天请求 (Chat Request)
     * @return Dify请求 (Dify Request)
     */
    private DifyRequest buildDifyRequest(ChatRequest request) {
        // 构建输入参数 (Build input parameters)
        Map<String, String> inputs = new HashMap<>();
        // 只在inputs中添加必要参数，避免向inputs中放入全部参数
        
        // 构建用户信息 (Build user info)
        String userId = UUID.randomUUID().toString();
        
        // 构建请求 (Build request)
        return DifyRequest.builder()
                .inputs(inputs)
                .query(request.getMessage())
                .user(userId)
                .conversationId(request.getSessionId())
                .responseMode("streaming")
                .build();
    }
    
    /**
     * 构建HTTP头
     * (Build HTTP Headers)
     *
     * @return HTTP头 (HTTP Headers)
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 设置API密钥 (Set API Key)
        String apiKey = difyApiConfig.getApiKey();
        if (apiKey == null || apiKey.equals("your-api-key-here")) {
            // 使用本地开发的默认API密钥 (Use default API key for local development)
            apiKey = "sk-1234567890abcdef1234567890abcdef";
            log.warn("使用默认API密钥进行开发测试。生产环境请设置正确的API密钥。(Using default API key for development testing. Please set the correct API key in production environment.)");
        }
        headers.set("Authorization", "Bearer " + apiKey);
        
        // 如果启用了HMAC签名，添加HMAC签名头 (Add HMAC signature header if enabled)
        if (difyApiConfig.getHmac().isEnabled()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String data = timestamp + difyApiConfig.getServiceName();
            String signature = hmacUtils.sign(data, difyApiConfig.getHmac().getSecretKey());
            
            headers.set("X-Signature-Timestamp", timestamp);
            headers.set("X-Signature", signature);
        }
        
        return headers;
    }
    
    /**
     * 解析操作指令
     * (Parse Action Command)
     *
     * @param action 操作 (Action)
     * @return 操作指令 (Action Command)
     */
    private ChatResponse.ActionCommand parseAction(Map<String, Object> action) {
        if (action == null || action.isEmpty()) {
            return null;
        }
        
        try {
            // 提取操作类型和目标 (Extract action type and target)
            String actionType = (String) action.getOrDefault("type", "");
            String target = (String) action.getOrDefault("target", "");
            
            // 提取参数 (Extract parameters)
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) action.getOrDefault("params", new HashMap<>());
            
            return ChatResponse.ActionCommand.builder()
                    .type(actionType)
                    .target(target)
                    .params(params)
                    .build();
        } catch (Exception e) {
            log.error("解析操作指令失败 (Failed to parse action command)", e);
            return null;
        }
    }
    
    /**
     * 将ActionCommand对象转换为Map
     * (Convert ActionCommand to Map)
     *
     * @param actionCommand 操作指令对象
     * @return 操作指令Map
     */
    private Map<String, Object> convertActionCommandToMap(ChatResponse.ActionCommand actionCommand) {
        if (actionCommand == null) {
            return null;
        }
        
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("type", actionCommand.getType());
        if (actionCommand.getTarget() != null) {
            actionMap.put("target", actionCommand.getTarget());
        }
        if (actionCommand.getParams() != null) {
            actionMap.put("params", actionCommand.getParams());
        }
        return actionMap;
    }
    
    /**
     * 从流式响应中提取最后一个有效的消息对象
     * (Extract the last valid message object from streaming response)
     *
     * @param streamResponse 流式响应文本 (Streaming response text)
     * @return 最后一个有效的消息对象 (Last valid message object)
     */
    private DifyResponse extractLastValidMessage(String streamResponse) {
        if (streamResponse == null || streamResponse.isEmpty()) {
            return null;
        }
        
        // 将响应按"data: "分割，每个块可能包含一个JSON对象
        // (Split response by "data: ", each block may contain a JSON object)
        String[] chunks = streamResponse.split("data: ");
        
        DifyResponse lastValidMessage = null;
        
        // 从后向前查找有效的消息对象（不是message_end事件）
        // (Search for valid message objects from back to front, not message_end events)
        for (int i = chunks.length - 1; i >= 0; i--) {
            String chunk = chunks[i].trim();
            if (chunk.isEmpty()) continue;
            
            // 如果是事件类型行，跳过
            // (Skip if it's an event type line)
            if (chunk.startsWith("event:")) continue;
            
            try {
                // 尝试解析为DifyResponse对象
                // (Try to parse as DifyResponse object)
                DifyResponse message = objectMapper.readValue(chunk, DifyResponse.class);
                
                // 找到最后一个有效消息（包含会话ID但不是message_end事件）
                // (Find the last valid message with conversation ID but not message_end event)
                if (message.getConversationId() != null && 
                    !("message_end".equals(message.getEvent()) && message.getAnswer() == null)) {
                    lastValidMessage = message;
                    break;
                }
            } catch (Exception e) {
                // 当前块不是有效的JSON或不是DifyResponse格式，继续查找
                // (Current block is not valid JSON or not in DifyResponse format, continue searching)
                continue;
            }
        }
        
        return lastValidMessage;
    }
    
    /**
     * 从流式响应中提取并合并所有回答内容
     * (Extract and combine all answer content from streaming response)
     *
     * @param streamResponse 流式响应文本 (Streaming response text)
     * @return 合并后的回答内容 (Combined answer content)
     */
    private String extractCombinedAnswerFromStream(String streamResponse) {
        if (streamResponse == null || streamResponse.isEmpty()) {
            return null;
        }
        
        // 将响应按"data: "分割，每个块可能包含一个JSON对象
        // (Split response by "data: ", each block may contain a JSON object)
        String[] chunks = streamResponse.split("data: ");
        
        StringBuilder combinedAnswer = new StringBuilder();
        
        // 从前向后遍历所有消息块，提取并合并答案内容
        // (Traverse all message blocks from front to back, extract and combine answer content)
        for (String chunk : chunks) {
            chunk = chunk.trim();
            if (chunk.isEmpty()) continue;
            
            // 如果是事件类型行，跳过
            // (Skip if it's an event type line)
            if (chunk.startsWith("event:")) continue;
            
            try {
                // 尝试解析为JsonNode对象，这样我们可以只提取answer字段
                // (Try to parse as JsonNode object so we can extract only the answer field)
                JsonNode jsonNode = objectMapper.readTree(chunk);
                
                // 提取answer字段，如果不为null或空，添加到合并结果中
                // (Extract answer field, if not null or empty, add to combined result)
                if (jsonNode.has("answer") && !jsonNode.get("answer").isNull()) {
                    String answerPart = jsonNode.get("answer").asText();
                    if (answerPart != null && !answerPart.isEmpty()) {
                        combinedAnswer.append(answerPart);
                    }
                }
            } catch (Exception e) {
                // 当前块不是有效的JSON，继续处理下一个
                // (Current block is not valid JSON, continue to next one)
                continue;
            }
        }
        
        return combinedAnswer.length() > 0 ? combinedAnswer.toString() : null;
    }
    
    /**
     * 从流式响应中提取最后一个完整的JSON对象
     * (Extract the last complete JSON object from streaming response)
     *
     * @param streamResponse 流式响应文本 (Streaming response text)
     * @return 最后一个完整的JSON对象字符串 (Last complete JSON object string)
     */
    private String extractLastCompleteJson(String streamResponse) {
        if (streamResponse == null || streamResponse.isEmpty()) {
            return null;
        }
        
        // 将响应按"data: "分割，每个块可能包含一个JSON对象
        // (Split response by "data: ", each block may contain a JSON object)
        String[] chunks = streamResponse.split("data: ");
        
        // 从后向前查找有效的JSON对象
        // (Search for valid JSON objects from back to front)
        for (int i = chunks.length - 1; i >= 0; i--) {
            String chunk = chunks[i].trim();
            if (chunk.isEmpty()) continue;
            
            // 如果是事件类型行，跳过
            // (Skip if it's an event type line)
            if (chunk.startsWith("event:")) continue;
            
            try {
                // 尝试解析为JSON，如果成功则返回
                // (Try to parse as JSON, return if successful)
                objectMapper.readTree(chunk);
                return chunk;
            } catch (Exception e) {
                // 当前块不是有效的JSON，继续查找
                // (Current block is not valid JSON, continue searching)
                continue;
            }
        }
        
        return null;
    }
} 