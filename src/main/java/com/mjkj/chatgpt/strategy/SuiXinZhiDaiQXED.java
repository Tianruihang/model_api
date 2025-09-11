package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class SuiXinZhiDaiQXED implements KeywordStrategy{
    private static final String[] KEYWORDS = {"随心智贷取消额度",  "随心智贷取消额度",
            "随心智贷额度取消", "随心智贷额度撤销", "随心智贷额度撤回","随心自带额度取消","随心自带额度撤销",
            "随心自带额度撤回","随心自带额度取消原因","随心自带额度撤销原因","随心自带额度撤回原因"};

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
        return "随心智贷想取消额度，该如何处理？";
    }
}
