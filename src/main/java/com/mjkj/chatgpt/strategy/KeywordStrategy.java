package com.mjkj.chatgpt.strategy;

public interface KeywordStrategy {
    boolean matches(String prompt); // 判断是否匹配关键词
    String getKeywordType(); // 返回关键词类型标识
}
