package com.mjkj.chatgpt.model;

import lombok.Data;

/**
 * 简化语义匹配结果模型
 */
@Data
public class SimpleSemanticResult {
    
    /**
     * 匹配到的答案
     */
    private String answer;
    
    /**
     * 相似度分数
     */
    private Double similarity;
    
    /**
     * 是否匹配成功
     */
    private Boolean matched;
    
    /**
     * 匹配原因
     */
    private String matchReason;
}
