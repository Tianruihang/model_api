package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class AnswerFalseStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {"错误","答案是错误", "错的", "答案是错的", "错误的", "不是的", "不是对的", "不是正确的", "不是对的"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "错误";
    }
}
