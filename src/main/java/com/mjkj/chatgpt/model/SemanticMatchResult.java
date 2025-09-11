package com.mjkj.chatgpt.model;

import lombok.Data;

/**
 * 语义匹配结果模型
 */
@Data
public class SemanticMatchResult {
    
    /**
     * 匹配的问答
     */
    private DailyQuestionModel dailyQuestion;
    
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
