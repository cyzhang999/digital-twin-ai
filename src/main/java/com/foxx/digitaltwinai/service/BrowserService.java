package com.foxx.digitaltwinai.service;

import java.util.Map;

/**
 * 浏览器操作服务接口
 * (Browser operation service interface)
 */
public interface BrowserService {
    
    /**
     * 执行模型操作
     * (Execute model operation)
     *
     * @param action 操作数据 (Action data)
     * @return 操作结果 (Operation result)
     */
    Map<String, Object> executeModelOperation(Map<String, Object> action);
    
    /**
     * 执行旋转操作
     * (Execute rotation operation)
     *
     * @param parameters 旋转参数 (Rotation parameters)
     * @return 操作结果 (Operation result)
     */
    Map<String, Object> executeRotate(Map<String, Object> parameters);
    
    /**
     * 执行缩放操作
     * (Execute zoom operation)
     *
     * @param parameters 缩放参数 (Zoom parameters)
     * @return 操作结果 (Operation result)
     */
    Map<String, Object> executeZoom(Map<String, Object> parameters);
    
    /**
     * 执行聚焦操作
     * (Execute focus operation)
     *
     * @param target 目标部件 (Target component)
     * @return 操作结果 (Operation result)
     */
    Map<String, Object> executeFocus(String target);
    
    /**
     * 执行重置操作
     * (Execute reset operation)
     *
     * @return 操作结果 (Operation result)
     */
    Map<String, Object> executeReset();
} 