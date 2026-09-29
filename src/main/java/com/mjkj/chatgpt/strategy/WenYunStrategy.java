package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class WenYunStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {
            "你好万维", "你好小维",
            "你好，万维", "你好，小维",
            "万维你好", "小维你好",
            "小维",
             "嗨小维",
             "嗨，小维",
            "哈喽万维", "哈喽小维",
            "哈喽，万维", "哈喽，小维",
            "您好万维", "您好小维",
            "您好，万维", "您好，小维",
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
        return "万维";
    }
}