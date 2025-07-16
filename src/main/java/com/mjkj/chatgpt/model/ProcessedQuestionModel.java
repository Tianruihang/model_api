package com.mjkj.chatgpt.model;

import lombok.Data;
import java.util.Date;

@Data
public class ProcessedQuestionModel {
    
    private int id;
    // 原始问题ID
    private int originalQuestionId;
    // 清洗后的问题内容
    private String cleanedQuestion;
    // 分类ID
    private Integer categoryId;
    // 分类名称
    private String categoryName;
    // 置信度（0-1）
    private Double confidence;
    // 处理时间
    private Date processDate;
    // 创建时间
    private Date createDate;
} 