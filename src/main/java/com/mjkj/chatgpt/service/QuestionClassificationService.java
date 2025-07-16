package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.mapper.QuestionCategoryMapper;
import com.mjkj.chatgpt.model.QuestionCategoryModel;
import com.mjkj.chatgpt.model.ProcessedQuestionModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionClassificationService {

    @Autowired
    private QuestionCategoryMapper categoryMapper;

    @Autowired
    private DataCleaningService dataCleaningService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分类单个问题
     * @param question 原始问题
     * @param originalQuestionId 原始问题ID
     * @return 处理后的问题模型
     */
    public ProcessedQuestionModel classifyQuestion(String question, int originalQuestionId) {
        // 清洗问题
        String cleanedQuestion = dataCleaningService.cleanQuestion(question);
        
        if (cleanedQuestion.isEmpty()) {
            return createUnclassifiedQuestion(originalQuestionId, cleanedQuestion);
        }

        // 获取所有分类
        List<QuestionCategoryModel> categories = categoryMapper.selectAllEnabledCategories();
        
        // 找到最佳匹配的分类
        ClassificationResult result = findBestMatch(cleanedQuestion, categories);
        
        // 创建处理后的问题模型
        ProcessedQuestionModel processedQuestion = new ProcessedQuestionModel();
        processedQuestion.setOriginalQuestionId(originalQuestionId);
        processedQuestion.setCleanedQuestion(cleanedQuestion);
        processedQuestion.setCategoryId(result.getCategoryId());
        processedQuestion.setCategoryName(result.getCategoryName());
        processedQuestion.setConfidence(result.getConfidence());
        processedQuestion.setProcessDate(new Date());
        processedQuestion.setCreateDate(new Date());
        
        return processedQuestion;
    }

    /**
     * 批量分类问题
     * @param questions 问题列表
     * @return 处理后的问题列表
     */
    public List<ProcessedQuestionModel> batchClassifyQuestions(List<Map<String, Object>> questions) {
        List<ProcessedQuestionModel> processedQuestions = new ArrayList<>();
        
        for (Map<String, Object> questionData : questions) {
            String question = (String) questionData.get("question");
            Integer id = (Integer) questionData.get("id");
            
            if (question != null && id != null) {
                ProcessedQuestionModel processed = classifyQuestion(question, id);
                processedQuestions.add(processed);
            }
        }
        
        return processedQuestions;
    }

    /**
     * 查找最佳匹配的分类
     * @param cleanedQuestion 清洗后的问题
     * @param categories 分类列表
     * @return 分类结果
     */
    private ClassificationResult findBestMatch(String cleanedQuestion, List<QuestionCategoryModel> categories) {
        ClassificationResult bestMatch = new ClassificationResult();
        bestMatch.setConfidence(0.0);
        bestMatch.setCategoryName("未分类");

        for (QuestionCategoryModel category : categories) {
            try {
                // 解析关键词
                List<String> keywords = parseKeywords(category.getKeywords());
                
                // 计算匹配度
                double confidence = calculateConfidence(cleanedQuestion, keywords);
                
                if (confidence > bestMatch.getConfidence()) {
                    bestMatch.setCategoryId(category.getId());
                    bestMatch.setCategoryName(category.getCategoryName());
                    bestMatch.setConfidence(confidence);
                }
            } catch (Exception e) {
                // 忽略解析错误
                continue;
            }
        }

        return bestMatch;
    }

    /**
     * 计算匹配度
     * @param question 问题
     * @param keywords 关键词列表
     * @return 匹配度（0-1）
     */
    private double calculateConfidence(String question, List<String> keywords) {
        if (keywords.isEmpty()) {
            return 0.0;
        }

        int matchCount = 0;
        String[] questionWords = question.toLowerCase().split("\\s+");
        
        for (String keyword : keywords) {
            for (String word : questionWords) {
                if (word.contains(keyword.toLowerCase()) || keyword.toLowerCase().contains(word)) {
                    matchCount++;
                    break;
                }
            }
        }

        return (double) matchCount / keywords.size();
    }

    /**
     * 解析关键词JSON
     * @param keywordsJson 关键词JSON字符串
     * @return 关键词列表
     */
    private List<String> parseKeywords(String keywordsJson) {
        try {
            return objectMapper.readValue(keywordsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 创建未分类的问题
     * @param originalQuestionId 原始问题ID
     * @param cleanedQuestion 清洗后的问题
     * @return 处理后的问题模型
     */
    private ProcessedQuestionModel createUnclassifiedQuestion(int originalQuestionId, String cleanedQuestion) {
        ProcessedQuestionModel processedQuestion = new ProcessedQuestionModel();
        processedQuestion.setOriginalQuestionId(originalQuestionId);
        processedQuestion.setCleanedQuestion(cleanedQuestion);
        processedQuestion.setCategoryId(null);
        processedQuestion.setCategoryName("未分类");
        processedQuestion.setConfidence(0.0);
        processedQuestion.setProcessDate(new Date());
        processedQuestion.setCreateDate(new Date());
        return processedQuestion;
    }

    /**
     * 分类结果内部类
     */
    private static class ClassificationResult {
        private Integer categoryId;
        private String categoryName;
        private double confidence;

        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
} 