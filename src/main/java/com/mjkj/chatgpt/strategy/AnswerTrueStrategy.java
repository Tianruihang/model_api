package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class AnswerTrueStrategy implements KeywordStrategy{


    private static final String[] KEYWORDS = {"正确","答案是正确","对的","答案是对的","正确的","对的","是的","是对的","是正确的","是对的"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "正确";
    }
}
