package com.mjkj.chatgpt.strategy;

public class DefaultStrategy implements KeywordStrategy{

    @Override
    public boolean matches(String prompt) {
        return true; // 默认匹配所有
    }

    @Override
    public String getKeywordType() {
        return "其他";
    }
}
