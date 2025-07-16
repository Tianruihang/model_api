package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.model.BankQuestionModel;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface BankQuestionService {
    //插入
    int insertQuestion(BankQuestionModel bankQuestionModel);

    //查询
    List<BankQuestionModel> getBankQuestionPage(
        Date startDate,
        Date endDate,
       int pageNum,
        int pageSize
    );
}
