package com.foxx.digitaltwinai.service.impl;

import com.foxx.digitaltwinai.entity.DifyRequestLog;
import com.foxx.digitaltwinai.repository.DifyRequestLogRepository;
import com.foxx.digitaltwinai.service.DifyRequestLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dify请求日志服务实现类
 * (Dify Request Log Service Implementation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyRequestLogServiceImpl implements DifyRequestLogService {

    private final DifyRequestLogRepository difyRequestLogRepository;

    @Override
    public DifyRequestLog saveLog(DifyRequestLog logEntity) {
        logEntity.setCreatedAt(LocalDateTime.now());
        DifyRequestLog saved = difyRequestLogRepository.save(logEntity);
        log.info("保存Dify请求日志成功, ID: {}", saved.getId());
        return saved;
    }

    /**
     * 记录请求数据
     * Record request data
     */
    @Override
    public DifyRequestLog recordRequest(String userInstruction, String requestContent, String responseContent,
                        Long responseTime, Integer status, String errorMessage,
                        String operationType, String targetComponent) {
        // 参数有效性验证
        try {
            // 防空检查 - 用户指令不能为空
            if (userInstruction == null || userInstruction.trim().isEmpty()) {
                userInstruction = "无用户指令";  // 设置默认值，避免数据库约束错误
            }
            
            // 请求内容空值检查
            if (requestContent == null) {
                requestContent = "无请求内容";
            }
            
            // 确保状态值有效
            if (status == null) {
                status = 0;  // 默认为失败状态
            }
            
            // 创建并保存日志记录
            DifyRequestLog log = new DifyRequestLog();
            log.setUserInstruction(userInstruction);
            log.setRequestContent(requestContent);
            log.setResponseContent(responseContent);
            log.setResponseTime(responseTime);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setOperationType(operationType);
            log.setTargetComponent(targetComponent);
            log.setCreatedAt(LocalDateTime.now());
            
            return saveLog(log);
        } catch (Exception e) {
            // 记录日志错误，但不影响调用方
            log.error("记录请求日志时出错: {}", e.getMessage(), e);
            
            // 即使出错，仍尝试创建一个最小化的日志记录
            DifyRequestLog fallbackLog = new DifyRequestLog();
            fallbackLog.setUserInstruction(userInstruction != null ? userInstruction : "错误记录");
            fallbackLog.setErrorMessage("记录日志时出错: " + e.getMessage());
            fallbackLog.setStatus(0); // 失败状态
            fallbackLog.setCreatedAt(LocalDateTime.now());
            
            try {
                return saveLog(fallbackLog);
            } catch (Exception ex) {
                log.error("创建备用日志记录也失败了: {}", ex.getMessage());
                return null; // 最终无法保存时返回null
            }
        }
    }

    @Override
    public List<DifyRequestLog> getLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        log.debug("查询时间范围内的日志: {} 至 {}", start, end);
        return difyRequestLogRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public List<DifyRequestLog> getLogsByOperationType(String operationType) {
        log.debug("查询操作类型的日志: {}", operationType);
        return difyRequestLogRepository.findByOperationType(operationType);
    }

    @Override
    public List<DifyRequestLog> getLogsByTargetComponent(String targetComponent) {
        log.debug("查询目标部件的日志: {}", targetComponent);
        return difyRequestLogRepository.findByTargetComponent(targetComponent);
    }

    @Override
    public List<DifyRequestLog> getFailedLogs() {
        log.debug("查询失败的请求日志");
        return difyRequestLogRepository.findByStatus(0);
    }
} 