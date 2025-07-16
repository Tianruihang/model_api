package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.mapper.ProcessedQuestionMapper;
import com.mjkj.chatgpt.mapper.QuestionStatisticsMapper;
import com.mjkj.chatgpt.mapper.QuestionCategoryMapper;
import com.mjkj.chatgpt.model.ProcessedQuestionModel;
import com.mjkj.chatgpt.model.QuestionStatisticsModel;
import com.mjkj.chatgpt.model.QuestionCategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionStatisticsService {

    @Autowired
    private ProcessedQuestionMapper processedQuestionMapper;

    @Autowired
    private QuestionStatisticsMapper statisticsMapper;

    @Autowired
    private QuestionCategoryMapper categoryMapper;

    /**
     * 生成统计数据
     * @param statisticsDate 统计日期
     * @return 统计结果列表
     */
    public List<QuestionStatisticsModel> generateStatistics(Date statisticsDate) {
        // 获取所有处理后的问题
        List<ProcessedQuestionModel> processedQuestions = processedQuestionMapper.selectAll();
        
        // 按分类分组统计
        Map<String, Long> categoryCounts = processedQuestions.stream()
                .filter(q -> q.getCategoryName() != null && !q.getCategoryName().equals("未分类"))
                .collect(Collectors.groupingBy(
                    ProcessedQuestionModel::getCategoryName,
                    Collectors.counting()
                ));

        // 1. 查询所有分类，建立映射
        List<QuestionCategoryModel> categories = categoryMapper.selectAllEnabledCategories();
        Map<String, Integer> categoryNameToId = categories.stream()
            .collect(Collectors.toMap(QuestionCategoryModel::getCategoryName, QuestionCategoryModel::getId));

        // 2. 生成统计数据
        List<QuestionStatisticsModel> statisticsList = new ArrayList<>();
        Date now = new Date();

        for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
            QuestionStatisticsModel statistics = new QuestionStatisticsModel();
            statistics.setCategoryName(entry.getKey());
            statistics.setQuestionCount(entry.getValue().intValue());
            statistics.setStatisticsDate(statisticsDate);
            statistics.setCreateDate(now);
            statistics.setUpdateDate(now);

            // 通过分类名查找ID
            Integer categoryId = categoryNameToId.get(entry.getKey());
            statistics.setCategoryId(categoryId); // 这里不再是null

            statisticsList.add(statistics);
        }

        return statisticsList;
    }

    /**
     * 保存统计数据
     * @param statisticsList 统计数据列表
     * @return 保存的记录数
     */
    public int saveStatistics(List<QuestionStatisticsModel> statisticsList) {
        if (statisticsList == null || statisticsList.isEmpty()) {
            return 0;
        }
        return statisticsMapper.batchInsertStatistics(statisticsList);
    }

    /**
     * 生成并保存统计数据
     * @param statisticsDate 统计日期
     * @return 保存的记录数
     */
    public int generateAndSaveStatistics(Date statisticsDate) {
        List<QuestionStatisticsModel> statisticsList = generateStatistics(statisticsDate);
        return saveStatistics(statisticsList);
    }

    /**
     * 获取最新统计数据
     * @return 最新统计数据列表
     */
    public List<QuestionStatisticsModel> getLatestStatistics() {
        return statisticsMapper.selectLatestStatistics();
    }

    /**
     * 根据日期范围获取统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    public List<QuestionStatisticsModel> getStatisticsByDateRange(Date startDate, Date endDate) {
        return statisticsMapper.selectStatisticsByDateRange(startDate, endDate);
    }

    /**
     * 获取所有统计数据
     * @return 所有统计数据列表
     */
    public List<QuestionStatisticsModel> getAllStatistics() {
        return statisticsMapper.selectAllStatistics();
    }

    /**
     * 获取分类统计摘要
     * @return 统计摘要
     */
    public Map<String, Object> getStatisticsSummary() {
        List<QuestionStatisticsModel> latestStats = getLatestStatistics();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCategories", latestStats.size());
        summary.put("totalQuestions", latestStats.stream()
                .mapToInt(QuestionStatisticsModel::getQuestionCount)
                .sum());
        summary.put("statistics", latestStats);
        
        return summary;
    }
} 