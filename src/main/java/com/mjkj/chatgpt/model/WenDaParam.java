package com.mjkj.chatgpt.model;

import lombok.Data;

@Data
public class WenDaParam {
    private String url;
    private String session_id;
    private String prompt; //问题
    private String step; //步骤
    private String memory_name; //记忆名称
}
