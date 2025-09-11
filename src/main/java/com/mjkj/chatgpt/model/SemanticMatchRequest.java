package com.mjkj.chatgpt.model;

import lombok.Data;

/**
 * 语义匹配请求模型
 */
@Data
public class SemanticMatchRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 匹配阈值（可选，默认0.6）
     */
    private Double threshold;
    
    /**
     * 最大返回结果数（可选，默认1）
     */
    private Integer maxResults;
}
