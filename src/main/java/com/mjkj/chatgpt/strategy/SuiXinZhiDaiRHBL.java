package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class SuiXinZhiDaiRHBL implements KeywordStrategy{
    private static final String[] KEYWORDS = {"随心智贷如何办理","随心智贷怎么办理"};

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
        return "随心智贷如何办理？";
    }
}
