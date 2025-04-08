package com.foxx.digitaltwinai.repository;

import com.foxx.digitaltwinai.entity.DifyRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dify请求日志仓库接口
 * (Dify Request Log Repository)
 */
@Repository
public interface DifyRequestLogRepository extends JpaRepository<DifyRequestLog, Long> {

    /**
     * 查找指定时间范围内的日志
     * (Find logs within a specified time range)
     * 
     * @param start 开始时间 (Start time)
     * @param end 结束时间 (End time)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 查找特定操作类型的日志
     * (Find logs of a specific operation type)
     * 
     * @param operationType 操作类型 (Operation type)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> findByOperationType(String operationType);
    
    /**
     * 查找特定目标部件的日志
     * (Find logs for a specific target component)
     * 
     * @param targetComponent 目标部件 (Target component)
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> findByTargetComponent(String targetComponent);
    
    /**
     * 查找处理失败的日志
     * (Find logs of failed processing)
     * 
     * @return 日志列表 (List of logs)
     */
    List<DifyRequestLog> findByStatus(Integer status);
} 