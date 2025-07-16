package com.mjkj.chatgpt.strategy;

public class WakeupXiaoyuanStrategy implements KeywordStrategy{

    @Override
    public boolean matches(String prompt) {
        return prompt.contains("唤醒小元");
    }

    @Override
    public String getKeywordType() {
        return "唤醒小元";
    }
}
