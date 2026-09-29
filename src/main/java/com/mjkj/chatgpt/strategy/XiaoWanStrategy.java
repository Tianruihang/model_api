package com.mjkj.chatgpt.strategy;

import com.mjkj.chatgpt.utils.PinyinUtils;

import java.util.Arrays;

public class XiaoWanStrategy implements KeywordStrategy {

    private static final String[] KEYWORDS = {
            "你好小智", "你好，小智",
            "小智你好", "小智",
            "嗨小智", "嗨，小智",
            "哈喽小智", "哈喽，小智",
            "您好小智", "您好，小智",
            
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
        return "小万";
    }
}