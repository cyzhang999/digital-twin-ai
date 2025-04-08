package com.foxx.digitaltwinai.service;

import com.foxx.digitaltwinai.entity.DifyRequestLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dify请求日志服务接口
 * (Dify Request Log Service Interface)
 */
public interface DifyRequestLogService {

    /**
     * 保存请求日志
     * (Save request log)
     * 
     * @param log 日志实体 (Log entity)
     * @return 保存的日志 (Saved log)
     */
    DifyRequestLog saveLog(DifyRequestLog log);
    
    /**
     * 记录请求和响应
     * (Record request and response)
     * 
     * @param userInstruction 用户指令 (User instruction)
     * @param requestContent 请求内容 (Request content)
     * @param responseContent 响应内容 (Response content)
     * @param responseTime 响应时间(毫秒) (Response time in ms)
     * @param status 状态(0失败,1成功) (Status: 0=failed, 1=success)
     * @param errorMessage 错误信息 (Error message)
     * @param operationType 操作类型 (Operation type)
     * @param targetComponent 目标部件 (Target component)
     * @return 保存的日志 (Saved log)
     */
    DifyRequestLog recordRequest(String userInstruction, String requestContent, 
                                String responseContent, Long responseTime, 
                                Integer status, String errorMessage,
                                String operationType, String targetComponent);
    
    /**
     * 获取指定时间范围内的日志
     * (Get logs within time range)
     * 
     * @param start 开始时间 (Start time)
     * @param end 结束时间 (End time)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> getLogsByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取特定操作类型的日志
     * (Get logs by operation type)
     * 
     * @param operationType 操作类型 (Operation type)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> getLogsByOperationType(String operationType);
    
    /**
     * 获取特定目标部件的日志
     * (Get logs by target component)
     * 
     * @param targetComponent 目标部件 (Target component)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> getLogsByTargetComponent(String targetComponent);
    
    /**
     * 获取失败的请求日志
     * (Get failed request logs)
     * 
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> getFailedLogs();
} 