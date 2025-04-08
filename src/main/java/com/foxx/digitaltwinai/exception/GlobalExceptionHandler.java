package com.foxx.digitaltwinai.exception;

import com.foxx.digitaltwinai.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * (Global Exception Handler)
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    /**
     * WebSocket服务
     * (WebSocket Service)
     */
    private final WebSocketService webSocketService;
    
    /**
     * 处理验证异常
     * (Handle Validation Exception)
     * 
     * @param ex 验证异常 (Validation Exception)
     * @return 错误响应 (Error Response)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        errors.put("success", false);
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("message", "验证失败 (Validation failed)");
        errors.put("errors", validationErrors);
        
        String errorMessage = "请求参数验证失败 (Request parameter validation failed)";
        webSocketService.sendError(errorMessage);
        log.error(errorMessage);
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * 处理运行时异常
     * (Handle Runtime Exception)
     * 
     * @param ex 运行时异常 (Runtime Exception)
     * @return 错误响应 (Error Response)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errors = new HashMap<>();
        
        errors.put("success", false);
        errors.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put("message", ex.getMessage());
        
        webSocketService.sendError(ex.getMessage());
        log.error("运行时异常 (Runtime exception)", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
    
    /**
     * 处理全局异常
     * (Handle Global Exception)
     * 
     * @param ex 异常 (Exception)
     * @return 错误响应 (Error Response)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        
        errors.put("success", false);
        errors.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put("message", "服务器内部错误 (Server internal error)");
        
        String errorMessage = "未处理的异常: " + ex.getMessage() + " (Unhandled exception: " + ex.getMessage() + ")";
        webSocketService.sendError(errorMessage);
        log.error(errorMessage, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
} 