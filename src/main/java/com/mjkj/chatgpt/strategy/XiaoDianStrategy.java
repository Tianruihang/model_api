package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class XiaoDianStrategy implements KeywordStrategy {

    private static final String[] KEYWORDS = {
            "你好小电", "你好，小电",
            "小电你好", "小电",
            "嗨小电", "嗨，小电",
            "哈喽小电", "哈喽，小电",
            "您好小电", "您好，小电",
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
        return "小电";
    }
}