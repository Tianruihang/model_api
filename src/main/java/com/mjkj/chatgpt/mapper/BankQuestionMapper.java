package com.mjkj.chatgpt.mapper;

import com.mjkj.chatgpt.model.BankQuestionModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface BankQuestionMapper {

    //插入问题
    //BankQuestionModel
    @Insert("insert into bank_question (question,create_date) values(#{question}, #{createDate})")
    @Options(useGeneratedKeys = true , keyProperty = "id", keyColumn = "id")
    int insertQuestion(BankQuestionModel bankQuestionModel);

    //分页查询数据,根据创建时间分页查询
    @Select({
        "SELECT id, question, question_counts, creat_date ",
        "FROM bank_question",
        "WHERE creat_date BETWEEN #{startDate} AND #{endDate}",
        "ORDER BY creat_date DESC",
        "LIMIT #{pageSize} OFFSET #{offset}"
    })
    List<BankQuestionModel> selectByCreateDate(
        @Param("startDate") Date startDate, 
        @Param("endDate") Date endDate,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    //查询所有问题
    @Select("select * from bank_question order by create_date desc")
    List<BankQuestionModel> selectAll();
}
