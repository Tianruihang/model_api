package com.mjkj.chatgpt.model;

import lombok.Data;

import java.util.Date;

@Data
public class BankQuestionModel {

    private int id;
    //问题
    private String question;
    //创建时间
    private Date createDate;
}
