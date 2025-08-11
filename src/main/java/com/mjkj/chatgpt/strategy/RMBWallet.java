package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class RMBWallet  implements KeywordStrategy{
    private static final String[] KEYWORDS = {"对公钱包","数字人民币对公钱包"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "对公钱包";
    }
}
