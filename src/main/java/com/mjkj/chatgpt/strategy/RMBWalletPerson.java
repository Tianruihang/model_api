package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class RMBWalletPerson implements KeywordStrategy{
    private static final String[] KEYWORDS = {"对私钱包","数字人民币对私钱包"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "对私钱包";
    }
}
