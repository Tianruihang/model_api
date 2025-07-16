package com.mjkj.chatgpt.model;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIModelParam {
    private String model; // 模型名称
    private List<OpenAIMessage> messages; // 消息列表
    private double temperature; // 温度
    private double top_p; // 采样温度
    private boolean stream; // 是否流式响应

}
