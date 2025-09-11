package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class SuiXinZhiDaiEDDJ implements KeywordStrategy{
    private static final String[] KEYWORDS = {"随心智贷额度冻结", "随心智贷额度冻结原因",
            "随心智贷额度被冻结", "随心智贷额度被锁定", "随心智贷额度被限制","随心自带额度冻结","随心自带额度冻结原因",
            "随心自带额度被冻结", "随心自带额度被锁定", "随心自带额度被限制","随心自带额度被冻结原因","随心自带额度被锁定原因",
            "随心自带额度被限制原因"};

    // 把 KEYWORDS 转成拼音备用
    private static final String[] KEYWORDS_PINYIN = Arrays.stream(KEYWORDS)
            .map(PinyinUtils::toPinyin)
            .toArray(String[]::new);
    @Override
    public boolean matches(String prompt) {
        String promptPinyin = PinyinUtils.toPinyin(prompt);
        return Arrays.stream(KEYWORDS_PINYIN).anyMatch(promptPinyin::contains);
    }
    @Override
    public String getKeywordType() {
        return "随心智贷页面显示我的额度已冻结，想继续用款，该如何处理？";
    }
}
