package com.mjkj.chatgpt.model;

import lombok.Data;
import java.util.Date;

@Data
public class QuestionCategoryModel {
    
    private int id;
    // 分类名称
    private String categoryName;
    // 分类描述
    private String categoryDescription;
    // 关键词（JSON格式存储）
    private String keywords;
    // 创建时间
    private Date createDate;
    // 更新时间
    private Date updateDate;
    // 是否启用
    private boolean enabled;
} 