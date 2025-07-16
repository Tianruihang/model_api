package com.mjkj.chatgpt.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class DataCleaningService {

    // 移除特殊字符的正则表达式
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^\\w\\s\\u4e00-\\u9fa5]");
    // 移除多余空格的正则表达式
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    // 移除数字的正则表达式
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("\\d+");

    /**
     * 清洗问题文本
     * @param question 原始问题
     * @return 清洗后的问题
     */
    public String cleanQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "";
        }

        String cleaned = question.trim();

        // 1. 转换为小写（保留中文）
        cleaned = cleaned.toLowerCase();

        // 2. 移除特殊字符（保留中文、英文、数字、空格）
        cleaned = SPECIAL_CHARS_PATTERN.matcher(cleaned).replaceAll(" ");

        // 3. 移除多余空格
        cleaned = MULTIPLE_SPACES_PATTERN.matcher(cleaned).replaceAll(" ");

        // 4. 移除纯数字
        cleaned = NUMBERS_PATTERN.matcher(cleaned).replaceAll("");

        // 5. 再次清理多余空格
        cleaned = MULTIPLE_SPACES_PATTERN.matcher(cleaned).replaceAll(" ");

        // 6. 移除首尾空格
        cleaned = cleaned.trim();

        return cleaned;
    }

    /**
     * 提取关键词
     * @param question 清洗后的问题
     * @return 关键词列表
     */
    public String[] extractKeywords(String question) {
        if (question == null || question.trim().isEmpty()) {
            return new String[0];
        }

        // 简单的关键词提取：按空格分割，过滤掉停用词
        String[] words = question.split("\\s+");
        String[] stopWords = {"的", "是", "在", "有", "和", "与", "或", "但", "而", "如果", "因为", "所以", "the", "a", "an", "and", "or", "but", "if", "because", "so"};

        return java.util.Arrays.stream(words)
                .filter(word -> word.length() > 1) // 过滤单字符
                .filter(word -> !java.util.Arrays.asList(stopWords).contains(word.toLowerCase()))
                .toArray(String[]::new);
    }

    /**
     * 标准化问题格式
     * @param question 原始问题
     * @return 标准化后的问题
     */
    public String normalizeQuestion(String question) {
        String cleaned = cleanQuestion(question);
        
        // 确保问题以问号结尾
        if (!cleaned.endsWith("?")) {
            cleaned += "?";
        }

        return cleaned;
    }
} 