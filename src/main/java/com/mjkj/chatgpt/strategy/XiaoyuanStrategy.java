package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class XiaoyuanStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {
            "你好小园", "你好小原", "你好小员", "你好小圆", "你好小袁",
            "你好小猿", "你好小缘", "你好小辕", "你好小媛", "你好小元", "你好小源",
            "你好，小袁", "你好，小猿", "你好，小圆", "你好，小园",
            "你好，小原", "你好，小元", "你好，小源", "你好，小辕",
    };

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "小元";
    }
}
