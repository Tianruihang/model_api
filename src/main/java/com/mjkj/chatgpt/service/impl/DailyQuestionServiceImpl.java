package com.mjkj.chatgpt.service.impl;

import com.mjkj.chatgpt.mapper.DailyQuestionMapper;
import com.mjkj.chatgpt.model.DailyQuestionModel;
import com.mjkj.chatgpt.service.DailyQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 日常问答服务实现类
 */
@Service
public class DailyQuestionServiceImpl implements DailyQuestionService {
    
    @Autowired
    private DailyQuestionMapper dailyQuestionMapper;
    
    @Override
    public boolean addDailyQuestion(DailyQuestionModel dailyQuestion) {
        if (dailyQuestion == null || dailyQuestion.getQuestion() == null || dailyQuestion.getAnswer() == null) {
            return false;
        }
        
        // 设置默认值
        dailyQuestion.setCreateTime(LocalDateTime.now());
        dailyQuestion.setUpdateTime(LocalDateTime.now());
        if (dailyQuestion.getStatus() == null) {
            dailyQuestion.setStatus(1); // 默认启用
        }
        if (dailyQuestion.getSortWeight() == null) {
            dailyQuestion.setSortWeight(0);
        }
        
        return dailyQuestionMapper.insert(dailyQuestion) > 0;
    }
    
    @Override
    public boolean updateDailyQuestion(DailyQuestionModel dailyQuestion) {
        if (dailyQuestion == null || dailyQuestion.getId() == null) {
            return false;
        }
        
        dailyQuestion.setUpdateTime(LocalDateTime.now());
        return dailyQuestionMapper.updateById(dailyQuestion) > 0;
    }
    
    @Override
    public boolean deleteDailyQuestion(Long id) {
        if (id == null) {
            return false;
        }
        return dailyQuestionMapper.deleteById(id) > 0;
    }
    
    @Override
    public DailyQuestionModel getDailyQuestionById(Long id) {
        if (id == null) {
            return null;
        }
        return dailyQuestionMapper.selectById(id);
    }
    
    @Override
    public List<DailyQuestionModel> getAllEnabledQuestions() {
        return dailyQuestionMapper.selectAllEnabled();
    }
    
    @Override
    public List<DailyQuestionModel> getQuestionsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllEnabledQuestions();
        }
        return dailyQuestionMapper.selectByCategory(category);
    }
    
    @Override
    public List<DailyQuestionModel> searchQuestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEnabledQuestions();
        }
        return dailyQuestionMapper.searchByKeyword(keyword.trim());
    }
    
    @Override
    public List<DailyQuestionModel> getQuestionsByPage(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        
        int offset = (page - 1) * size;
        return dailyQuestionMapper.selectByPage(offset, size);
    }
    
    @Override
    public int getTotalCount() {
        return dailyQuestionMapper.countTotal();
    }
    
    @Override
    public DailyQuestionModel getRandomQuestion() {
        List<DailyQuestionModel> questions = getAllEnabledQuestions();
        if (questions == null || questions.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        int randomIndex = random.nextInt(questions.size());
        return questions.get(randomIndex);
    }
}
