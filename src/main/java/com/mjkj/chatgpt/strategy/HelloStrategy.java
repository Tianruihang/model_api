package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class HelloStrategy implements KeywordStrategy{
    private static final String[] KEYWORDS = {"你好呀"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "你好";
    }
}
