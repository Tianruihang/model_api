package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class XiaolanStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {"小蓝小蓝", "小兰小兰", "小蓝，小蓝", "小兰，小兰"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "小蓝";
    }
}
