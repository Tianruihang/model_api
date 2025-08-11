package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class ZhongYinYiDaiZXTS implements KeywordStrategy{

    private static final String[] KEYWORDS = {"中银易贷咨询投诉","中银易贷咨询投诉渠道有哪些？"};

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
        return "中银易贷咨询投诉渠道有哪些？";
    }
}
