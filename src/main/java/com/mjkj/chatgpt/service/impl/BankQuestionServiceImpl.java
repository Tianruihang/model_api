package com.mjkj.chatgpt.service.impl;

import com.mjkj.chatgpt.mapper.BankQuestionMapper;
import com.mjkj.chatgpt.model.BankQuestionModel;
import com.mjkj.chatgpt.service.BankQuestionService;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BankQuestionServiceImpl implements BankQuestionService {

    @Resource
    private BankQuestionMapper bankQuestionMapper;

    @Override
    public int insertQuestion(BankQuestionModel bankQuestionModel) {
        return bankQuestionMapper.insertQuestion(bankQuestionModel);
    }

    @Override
    public List<BankQuestionModel> getBankQuestionPage(Date startDate, Date endDate, int pageNum, int pageSize) {
            return bankQuestionMapper.selectByCreateDate(
                startDate,
                endDate,
                (pageNum - 1) * pageSize,
                pageSize
            );
    }
}
