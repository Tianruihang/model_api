package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.model.DailyQuestionModel;
import java.util.List;

/**
 * 日常问答服务接口
 */
public interface DailyQuestionService {
    
    /**
     * 添加日常问答
     */
    boolean addDailyQuestion(DailyQuestionModel dailyQuestion);
    
    /**
     * 更新日常问答
     */
    boolean updateDailyQuestion(DailyQuestionModel dailyQuestion);
    
    /**
     * 删除日常问答
     */
    boolean deleteDailyQuestion(Long id);
    
    /**
     * 根据ID获取日常问答
     */
    DailyQuestionModel getDailyQuestionById(Long id);
    
    /**
     * 获取所有启用的日常问答
     */
    List<DailyQuestionModel> getAllEnabledQuestions();
    
    /**
     * 根据分类获取日常问答
     */
    List<DailyQuestionModel> getQuestionsByCategory(String category);
    
    /**
     * 搜索日常问答
     */
    List<DailyQuestionModel> searchQuestions(String keyword);
    
    /**
     * 分页获取日常问答
     */
    List<DailyQuestionModel> getQuestionsByPage(int page, int size);
    
    /**
     * 获取总数
     */
    int getTotalCount();
    
    /**
     * 随机获取一个日常问答
     */
    DailyQuestionModel getRandomQuestion();
}
