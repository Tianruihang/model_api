package com.mjkj.chatgpt.model;

import lombok.Data;

@Data
public class AnswerModel {
    private String question; // 问题
    private String answer; // 回答
    private String videoName; // 步骤
}
