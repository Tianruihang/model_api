package com.mjkj.chatgpt.service.impl;

import com.mjkj.chatgpt.model.DailyQuestionModel;
import com.mjkj.chatgpt.model.SemanticMatchRequest;
import com.mjkj.chatgpt.model.SemanticMatchResult;
import com.mjkj.chatgpt.model.SimpleSemanticRequest;
import com.mjkj.chatgpt.model.SimpleSemanticResult;
import com.mjkj.chatgpt.service.DailyQuestionService;
import com.mjkj.chatgpt.service.SemanticMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语义匹配服务实现类
 */
@Service
public class SemanticMatchServiceImpl implements SemanticMatchService {
    
    @Autowired
    private DailyQuestionService dailyQuestionService;
    
    // 默认匹配阈值
    private static final double DEFAULT_THRESHOLD = 0.6;
    
    // 默认最大返回结果数
    private static final int DEFAULT_MAX_RESULTS = 1;
    
    @Override
    public SemanticMatchResult matchQuestion(SemanticMatchRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return createEmptyResult("问题不能为空");
        }
        
        String userQuestion = request.getQuestion().trim();
        double threshold = request.getThreshold() != null ? request.getThreshold() : DEFAULT_THRESHOLD;
        
        // 获取所有启用的问答
        List<DailyQuestionModel> allQuestions = dailyQuestionService.getAllEnabledQuestions();
        if (allQuestions == null || allQuestions.isEmpty()) {
            return createEmptyResult("暂无可用问答数据");
        }
        
        // 计算相似度并排序
        List<SemanticMatchResult> results = allQuestions.stream()
                .map(question -> calculateMatchResult(userQuestion, question))
                .filter(result -> result.getSimilarity() >= threshold)
                .sorted((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()))
                .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            return createEmptyResult("未找到匹配的问答，请尝试换个说法");
        }
        
        return results.get(0);
    }
    
    @Override
    public List<SemanticMatchResult> matchQuestions(SemanticMatchRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String userQuestion = request.getQuestion().trim();
        double threshold = request.getThreshold() != null ? request.getThreshold() : DEFAULT_THRESHOLD;
        int maxResults = request.getMaxResults() != null ? request.getMaxResults() : DEFAULT_MAX_RESULTS;
        
        // 获取所有启用的问答
        List<DailyQuestionModel> allQuestions = dailyQuestionService.getAllEnabledQuestions();
        if (allQuestions == null || allQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 计算相似度并排序
        return allQuestions.stream()
                .map(question -> calculateMatchResult(userQuestion, question))
                .filter(result -> result.getSimilarity() >= threshold)
                .sorted((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    @Override
    public Double calculateSimilarity(String question1, String question2) {
        if (question1 == null || question2 == null) {
            return 0.0;
        }
        
        // 转换为小写并去除标点符号
        String q1 = normalizeText(question1);
        String q2 = normalizeText(question2);
        
        // 如果完全相同，返回最高相似度
        if (q1.equals(q2)) {
            return 1.0;
        }
        
        // 计算Jaccard相似度
        double jaccardSimilarity = calculateJaccardSimilarity(q1, q2);
        
        // 计算关键词匹配相似度
        double keywordSimilarity = calculateKeywordSimilarity(q1, q2);
        
        // 计算编辑距离相似度
        double editDistanceSimilarity = calculateEditDistanceSimilarity(q1, q2);
        
        // 综合计算最终相似度（加权平均）
        double finalSimilarity = jaccardSimilarity * 0.4 + keywordSimilarity * 0.4 + editDistanceSimilarity * 0.2;
        
        return Math.round(finalSimilarity * 100.0) / 100.0; // 保留两位小数
    }
    
    /**
     * 计算匹配结果
     */
    private SemanticMatchResult calculateMatchResult(String userQuestion, DailyQuestionModel dbQuestion) {
        SemanticMatchResult result = new SemanticMatchResult();
        result.setDailyQuestion(dbQuestion);
        
        double similarity = calculateSimilarity(userQuestion, dbQuestion.getQuestion());
        result.setSimilarity(similarity);
        result.setMatched(similarity >= 0.6);
        
        // 设置匹配原因
        if (similarity >= 0.9) {
            result.setMatchReason("问题高度相似");
        } else if (similarity >= 0.8) {
            result.setMatchReason("问题非常相似");
        } else if (similarity >= 0.7) {
            result.setMatchReason("问题比较相似");
        } else if (similarity >= 0.6) {
            result.setMatchReason("问题有一定相似性");
        } else {
            result.setMatchReason("问题相似度较低");
        }
        
        return result;
    }
    
    /**
     * 创建空结果
     */
    private SemanticMatchResult createEmptyResult(String reason) {
        SemanticMatchResult result = new SemanticMatchResult();
        result.setMatched(false);
        result.setSimilarity(0.0);
        result.setMatchReason(reason);
        return result;
    }
    
    /**
     * 文本标准化
     */
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[\\p{P}\\s]+", " ")
                .trim();
    }
    
    /**
     * 计算Jaccard相似度
     */
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * 计算关键词匹配相似度
     */
    private double calculateKeywordSimilarity(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        int matches = 0;
        int totalWords = Math.max(words1.length, words2.length);
        
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) || word1.contains(word2) || word2.contains(word1)) {
                    matches++;
                    break;
                }
            }
        }
        
        return totalWords == 0 ? 0.0 : (double) matches / totalWords;
    }
    
    /**
     * 计算编辑距离相似度
     */
    private double calculateEditDistanceSimilarity(String text1, String text2) {
        int distance = calculateLevenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }
    
    /**
     * 计算Levenshtein编辑距离
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    @Override
    public SimpleSemanticResult simpleMatch(SimpleSemanticRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return createSimpleEmptyResult("问题不能为空");
        }
        
        String userQuestion = request.getQuestion().trim();
        double threshold = request.getThreshold() != null ? request.getThreshold() : DEFAULT_THRESHOLD;
        
        // 获取所有启用的问答
        List<DailyQuestionModel> allQuestions = dailyQuestionService.getAllEnabledQuestions();
        if (allQuestions == null || allQuestions.isEmpty()) {
            return createSimpleEmptyResult("暂无可用问答数据");
        }
        
        // 计算相似度并排序
        List<SemanticMatchResult> results = allQuestions.stream()
                .map(question -> calculateMatchResult(userQuestion, question))
                .filter(result -> result.getSimilarity() >= threshold)
                .sorted((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()))
                .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            return createSimpleEmptyResult("抱歉我暂时不会，请尝试换个话题");
        }
        
        // 返回最佳匹配结果
        SemanticMatchResult bestMatch = results.get(0);
        return createSimpleResult(bestMatch);
    }
    
    /**
     * 创建简化的空结果
     */
    private SimpleSemanticResult createSimpleEmptyResult(String reason) {
        SimpleSemanticResult result = new SimpleSemanticResult();
        result.setMatched(false);
        result.setSimilarity(0.0);
        result.setMatchReason(reason);
        result.setAnswer("");
        return result;
    }
    
    /**
     * 创建简化的结果
     */
    private SimpleSemanticResult createSimpleResult(SemanticMatchResult matchResult) {
        SimpleSemanticResult result = new SimpleSemanticResult();
        result.setMatched(matchResult.getMatched());
        result.setSimilarity(matchResult.getSimilarity());
        result.setMatchReason(matchResult.getMatchReason());
        result.setAnswer(matchResult.getDailyQuestion().getAnswer());
        return result;
    }
}
