package com.mjkj.chatgpt.model;

import lombok.Data;
import java.util.Date;

@Data
public class QuestionStatisticsModel {
    
    private int id;
    // 分类ID
    private Integer categoryId;
    // 分类名称
    private String categoryName;
    // 问题数量
    private int questionCount;
    // 统计日期
    private Date statisticsDate;
    // 创建时间
    private Date createDate;
    // 更新时间
    private Date updateDate;
} 