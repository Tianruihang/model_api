package com.mjkj.chatgpt.strategy;

import java.util.Arrays;
import java.util.List;

public class KeywordStrategyFactory {

    private static final List<KeywordStrategy> strategies = Arrays.asList(
            new XiaolanStrategy(),
            new WakeupXiaoyuanStrategy(),
            new HelloStrategy(),
            new CeyanStrategy(),
            new AnswerFalseStrategy(),
            new AnswerTrueStrategy(),
            new DefaultStrategy() // 必须放在最后
    );

    public static String checkPrompt(String prompt) {
        return strategies.stream()
                .filter(strategy -> strategy.matches(prompt))
                .findFirst()
                .map(KeywordStrategy::getKeywordType)
                .orElse("其他");
    }
}
