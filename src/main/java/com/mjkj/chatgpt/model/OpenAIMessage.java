package com.mjkj.chatgpt.model;

import lombok.Data;

@Data
public class OpenAIMessage {
    private String role; // 消息角色，可能的值包括 "system", "user", "assistant"
    private String content; // 消息内容
}
