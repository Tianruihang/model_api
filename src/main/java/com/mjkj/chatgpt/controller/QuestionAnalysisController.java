package com.mjkj.chatgpt.controller;

import com.mjkj.chatgpt.model.ProcessedQuestionModel;
import com.mjkj.chatgpt.model.QuestionCategoryModel;
import com.mjkj.chatgpt.service.QuestionAnalysisService;
import com.mjkj.chatgpt.service.QuestionClassificationService;
import com.mjkj.chatgpt.service.QuestionStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/question-analysis")
public class QuestionAnalysisController {

    @Autowired
    private QuestionAnalysisService analysisService;

    @Autowired
    private QuestionClassificationService classificationService;

    @Autowired
    private QuestionStatisticsService statisticsService;

    /**
     * 处理所有问题
     * @return 处理结果
     */
    @PostMapping("/process-all")
    public Map<String, Object> processAllQuestions() {
        return analysisService.processAllQuestions();
    }

    /**
     * 处理指定日期范围的问题
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageSize 分页大小
     * @return 处理结果
     */
    @PostMapping("/process-by-date")
    public Map<String, Object> processQuestionsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "100") int pageSize) {
        return analysisService.processQuestionsByDateRange(startDate, endDate, pageSize);
    }

    /**
     * 获取统计摘要
     * @return 统计摘要
     */
    @GetMapping("/statistics/summary")
    public Map<String, Object> getStatisticsSummary() {
        return analysisService.getStatisticsSummary();
    }

    /**
     * 获取分类统计详情
     * @return 分类统计详情
     */
    @GetMapping("/statistics/category")
    public Map<String, Object> getCategoryStatistics() {
        return analysisService.getCategoryStatistics();
    }

    /**
     * 获取所有统计数据
     * @return 统计数据列表
     */
    @GetMapping("/statistics/all")
    public List<Map<String, Object>> getAllStatistics() {
        return statisticsService.getAllStatistics().stream()
                .map(stat -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", stat.getId());
                    map.put("categoryId", stat.getCategoryId());
                    map.put("categoryName", stat.getCategoryName());
                    map.put("questionCount", stat.getQuestionCount());
                    map.put("statisticsDate", stat.getStatisticsDate());
                    map.put("createDate", stat.getCreateDate());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据日期范围获取统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    @GetMapping("/statistics/by-date")
    public List<Map<String, Object>> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return statisticsService.getStatisticsByDateRange(startDate, endDate).stream()
                .map(stat -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", stat.getId());
                    map.put("categoryId", stat.getCategoryId());
                    map.put("categoryName", stat.getCategoryName());
                    map.put("questionCount", stat.getQuestionCount());
                    map.put("statisticsDate", stat.getStatisticsDate());
                    map.put("createDate", stat.getCreateDate());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 分类单个问题
     * @param question 问题内容
     * @param originalQuestionId 原始问题ID
     * @return 分类结果
     */
    @PostMapping("/classify")
    public Map<String, Object> classifyQuestion(
            @RequestParam String question,
            @RequestParam int originalQuestionId) {
        
        ProcessedQuestionModel result = classificationService.classifyQuestion(question, originalQuestionId);
        
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("originalQuestionId", result.getOriginalQuestionId());
        map.put("cleanedQuestion", result.getCleanedQuestion());
        map.put("categoryId", result.getCategoryId());
        map.put("categoryName", result.getCategoryName());
        map.put("confidence", result.getConfidence());
        map.put("processDate", result.getProcessDate());
        return map;
    }

    /**
     * 生成统计数据
     * @return 生成结果
     */
    @PostMapping("/generate-statistics")
    public Map<String, Object> generateStatistics() {
        Date statisticsDate = new Date();
        int count = statisticsService.generateAndSaveStatistics(statisticsDate);
        
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("generatedCount", count);
        map.put("statisticsDate", statisticsDate);
        map.put("message", "统计数据生成完成");
        return map;
    }
} 