package com.mjkj.chatgpt.strategy;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CreditCard  implements KeywordStrategy{

    private static final String[] KEYWORDS = {
        "信用卡申请的条件", "申请的条件", "信用卡申请条件", "申请信用卡的条件", "信用卡申请需要满足的条件"
    };

    @Override
    public boolean matches(String prompt) {
        return Arrays.stream(KEYWORDS).anyMatch(prompt::contains);
    }

    @Override
    public String getKeywordType() {
        return "信用卡申请的条件是什么？";
    }
}
