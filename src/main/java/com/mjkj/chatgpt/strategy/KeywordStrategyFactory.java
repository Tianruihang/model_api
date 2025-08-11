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
            new CreditCard(),
            new RMBWalletPerson(),
            new RMBWallet(),
            new ZhongYinYiDaiDKZT(),
            new ZhongYinYiDaiHK(),
            new ZhongYinYiDaiJXFS(),
            new ZhongYinYiDaiLL(),
            new ZhongYinYiDaiQX(),
            new ZhongYinYiDaiYQQS(),
            new ZhongYinYiDaiZXTS(),
            new SuiXinZhiDaiDKJE(),
            new SuiXinZhiDaiEDDJ(),
            new SuiXinZhiDaiDKQX(),
            new SuiXinZhiDaiQXED(),
            new SuiXinZhiDaiRHBL(),
            new SuiXinZhiDaiSYLL(),
            new SuiXinZhiDaiTQHK(),
            new SuiXinZhiDaiZDHK(),
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
