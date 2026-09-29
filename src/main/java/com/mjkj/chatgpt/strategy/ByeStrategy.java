package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class ByeStrategy implements KeywordStrategy{

    private static final String[] KEYWORDS = {
            "退下", "再见", "拜拜", "再会",
            "下次见", "再见啦", "拜拜了",
            "告辞", "先这样", "回见"
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
        return "再见";
    }
}