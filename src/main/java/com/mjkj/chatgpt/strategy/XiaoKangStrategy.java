package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class XiaoKangStrategy implements KeywordStrategy {

    private static final String[] KEYWORDS = {
            "你好小康", "你好，小康",
            "小康你好", "小康",
            "嗨小康", "嗨，小康",
            "哈喽小康", "哈喽，小康",
            "您好小康", "您好，小康",
    };

    private static final String[] KEYWORDS_PINYIN = Arrays.stream(KEYWORDS)
            .map(PinyinUtils::toPinyin)
            .toArray(String[]::new);

    @Override
    public boolean matches(String prompt) {
        String cleanedPrompt = prompt.replaceAll("[。，！？、；：.\\s]", "");
        String promptPinyin = PinyinUtils.toPinyin(cleanedPrompt);
        return Arrays.stream(KEYWORDS_PINYIN).anyMatch(promptPinyin::contains);
    }

    @Override
    public String getKeywordType() {
        return "小康";
    }
}