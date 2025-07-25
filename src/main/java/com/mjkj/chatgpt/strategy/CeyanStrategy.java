package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class CeyanStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {"我要挑战", "你好小元，我要挑战", "你好小袁，我要挑战", "你好，小圆，我要挑战","你好，小袁，我要挑战","我要挑战",
            "小元，我要挑战", "小袁，我要挑战", "小圆，我要挑战", "小袁我要挑战","小元我要挑战", "小圆我要挑战", "小元，开始挑战", "小袁，开始挑战", "小圆，开始挑战", "小袁开始挑战", "小元开始挑战", "小圆开始挑战",};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "测验";
    }
}
