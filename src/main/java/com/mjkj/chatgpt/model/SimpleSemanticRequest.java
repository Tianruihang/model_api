package com.mjkj.chatgpt.model;

import lombok.Data;

/**
 * 简化语义匹配请求模型
 */
@Data
public class SimpleSemanticRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 匹配阈值（可选，默认0.6）
     */
    private Double threshold;
}
