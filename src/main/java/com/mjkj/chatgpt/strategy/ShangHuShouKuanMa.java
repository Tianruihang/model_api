package com.mjkj.chatgpt.strategy;

import java.util.Arrays;

public class ShangHuShouKuanMa implements KeywordStrategy{
    private static final String[] KEYWORDS = {"商户收款码","个体工商户收款码", "个体户收款码", "个体工商户二维码收款码", "个体户二维码收款码"};

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "个体工商户收款码如何办理";
    }
}
