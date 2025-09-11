package com.mjkj.chatgpt.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 日常问答模型
 */
@Data
public class DailyQuestionModel {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 问题内容
     */
    private String question;
    
    /**
     * 答案内容
     */
    private String answer;
    
    /**
     * 问题分类（可选）
     */
    private String category;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否启用（1-启用，0-禁用）
     */
    private Integer status;
    
    /**
     * 排序权重
     */
    private Integer sortWeight;
    
    /**
     * 备注
     */
    private String remark;
}
