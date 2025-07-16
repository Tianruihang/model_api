package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.mapper.BankQuestionMapper;
import com.mjkj.chatgpt.mapper.ProcessedQuestionMapper;
import com.mjkj.chatgpt.model.BankQuestionModel;
import com.mjkj.chatgpt.model.ProcessedQuestionModel;
import com.mjkj.chatgpt.model.QuestionStatisticsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionAnalysisService {

    @Autowired
    private BankQuestionMapper bankQuestionMapper;

    @Autowired
    private ProcessedQuestionMapper processedQuestionMapper;

    @Autowired
    private QuestionClassificationService classificationService;

    @Autowired
    private QuestionStatisticsService statisticsService;

    @Autowired
    private DataCleaningService dataCleaningService;

    /**
     * 处理所有原始问题
     * @return 处理结果
     */
    public Map<String, Object> processAllQuestions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取所有原始问题
            List<BankQuestionModel> originalQuestions = getAllOriginalQuestions();
            result.put("totalOriginalQuestions", originalQuestions.size());
            
            // 2. 转换为Map格式用于分类
            List<Map<String, Object>> questionDataList = originalQuestions.stream()
                    .map(q -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", q.getId());
                        data.put("question", q.getQuestion());
                        return data;
                    })
                    .collect(Collectors.toList());
            
            // 3. 批量分类问题
            List<ProcessedQuestionModel> processedQuestions = classificationService.batchClassifyQuestions(questionDataList);
            result.put("totalProcessedQuestions", processedQuestions.size());
            
            // 4. 保存处理后的问题
            int savedCount = processedQuestionMapper.batchInsertProcessedQuestions(processedQuestions);
            result.put("savedProcessedQuestions", savedCount);
            
            // 5. 生成统计数据
            Date statisticsDate = new Date();
            int statisticsCount = statisticsService.generateAndSaveStatistics(statisticsDate);
            result.put("generatedStatistics", statisticsCount);
            
            result.put("success", true);
            result.put("message", "问题处理完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "处理失败: " + e.getMessage());
            result.put("error", e);
        }
        
        return result;
    }

    /**
     * 处理指定日期范围的问题
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageSize 分页大小
     * @return 处理结果
     */
    public Map<String, Object> processQuestionsByDateRange(Date startDate, Date endDate, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int offset = 0;
            int totalProcessed = 0;
            List<ProcessedQuestionModel> allProcessedQuestions = new ArrayList<>();
            
            while (true) {
                // 分页获取原始问题
                List<BankQuestionModel> questions = bankQuestionMapper.selectByCreateDate(startDate, endDate, offset, pageSize);
                
                if (questions.isEmpty()) {
                    break;
                }
                
                // 转换为Map格式
                List<Map<String, Object>> questionDataList = questions.stream()
                        .map(q -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", q.getId());
                            data.put("question", q.getQuestion());
                            return data;
                        })
                        .collect(Collectors.toList());
                
                // 分类问题
                List<ProcessedQuestionModel> processedQuestions = classificationService.batchClassifyQuestions(questionDataList);
                allProcessedQuestions.addAll(processedQuestions);
                
                totalProcessed += questions.size();
                offset += pageSize;
                
                // 如果获取的数据少于pageSize，说明已经到最后一页
                if (questions.size() < pageSize) {
                    break;
                }
            }
            
            // 保存处理后的问题
            int savedCount = processedQuestionMapper.batchInsertProcessedQuestions(allProcessedQuestions);
            
            // 生成统计数据
            Date statisticsDate = new Date();
            int statisticsCount = statisticsService.generateAndSaveStatistics(statisticsDate);
            
            result.put("success", true);
            result.put("totalProcessed", totalProcessed);
            result.put("savedCount", savedCount);
            result.put("statisticsCount", statisticsCount);
            result.put("message", "指定日期范围的问题处理完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "处理失败: " + e.getMessage());
            result.put("error", e);
        }
        
        return result;
    }

    /**
     * 获取所有原始问题
     * @return 原始问题列表
     */
    private List<BankQuestionModel> getAllOriginalQuestions() {
        // 这里需要根据实际情况实现，暂时返回空列表
        // 你可以根据BankQuestionMapper的实际方法来实现
        List<BankQuestionModel> questions = bankQuestionMapper.selectAll();
        return questions;
    }

    /**
     * 获取统计摘要
     * @return 统计摘要
     */
    public Map<String, Object> getStatisticsSummary() {
        return statisticsService.getStatisticsSummary();
    }

    /**
     * 获取分类统计详情
     * @return 分类统计详情
     */
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取最新统计数据
            List<QuestionStatisticsModel> latestStats = statisticsService.getLatestStatistics();
            
            // 按分类名称分组
            Map<String, Integer> categoryCounts = latestStats.stream()
                    .collect(Collectors.toMap(
                        QuestionStatisticsModel::getCategoryName,
                        QuestionStatisticsModel::getQuestionCount
                    ));
            
            result.put("success", true);
            result.put("categoryCounts", categoryCounts);
            result.put("totalCategories", latestStats.size());
            result.put("totalQuestions", latestStats.stream()
                    .mapToInt(QuestionStatisticsModel::getQuestionCount)
                    .sum());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计详情失败: " + e.getMessage());
            result.put("error", e);
        }
        
        return result;
    }

    /**
     * 获取指定分类的问题列表
     * @param categoryName 分类名称
     * @return 问题列表
     */
    public List<ProcessedQuestionModel> getQuestionsByCategory(String categoryName) {
        // 这里需要根据分类名称查找分类ID，然后查询问题
        // 暂时返回空列表，需要根据实际Mapper方法实现
        return new ArrayList<>();
    }
} 