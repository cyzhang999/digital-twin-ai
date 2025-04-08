package com.foxx.digitaltwinai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dify请求日志实体类
 * (Dify Request Log Entity)
 */
@Entity
@Table(name = "dify_request_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户原始指令 (User original instruction)
    @Column(length = 500, nullable = false)
    private String userInstruction;

    // 请求内容 (Request content)
    @Column(columnDefinition = "TEXT")
    private String requestContent;

    // 响应内容 (Response content)
    @Column(columnDefinition = "TEXT")
    private String responseContent;

    // 响应时间(毫秒) (Response time in ms)
    private Long responseTime;

    // 处理状态 (Processing status: 0-失败 Failed, 1-成功 Success)
    private Integer status;

    // 错误信息 (Error message)
    @Column(length = 500)
    private String errorMessage;

    // 操作类型 (Operation type: rotate, zoom, focus, reset)
    @Column(length = 50)
    private String operationType;

    // 目标部件 (Target component)
    @Column(length = 100)
    private String targetComponent;

    // 创建时间 (Creation time)
    private LocalDateTime createdAt;

    // 实例化时自动设置创建时间 (Automatically set creation time)
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 