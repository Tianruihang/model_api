package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class XiaoGuangStrategy implements KeywordStrategy {

    private static final String[] KEYWORDS = {
            "你好小光", "你好，小光",
            "小光你好", "小光",
            "嗨小光", "嗨，小光",
            "哈喽小光", "哈喽，小光",
            "您好小光", "您好，小光",
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
        return "小光";
    }
}