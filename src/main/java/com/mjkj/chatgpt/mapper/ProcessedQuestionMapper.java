package com.mjkj.chatgpt.mapper;

import com.mjkj.chatgpt.model.ProcessedQuestionModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ProcessedQuestionMapper {

    // 插入处理后的问题
    @Insert("INSERT INTO processed_question (original_question_id, cleaned_question, category_id, category_name, " +
            "confidence, process_date, create_date) VALUES (#{originalQuestionId}, #{cleanedQuestion}, #{categoryId}, " +
            "#{categoryName}, #{confidence}, #{processDate}, #{createDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertProcessedQuestion(ProcessedQuestionModel processedQuestion);

    // 批量插入处理后的问题
    @Insert({
        "<script>",
        "INSERT INTO processed_question (original_question_id, cleaned_question, category_id, category_name, confidence, process_date, create_date) VALUES",
        "<foreach collection='list' item='item' separator=','>",
        "(#{item.originalQuestionId}, #{item.cleanedQuestion}, #{item.categoryId}, #{item.categoryName}, #{item.confidence}, #{item.processDate}, #{item.createDate})",
        "</foreach>",
        "</script>"
    })
    int batchInsertProcessedQuestions(@Param("list") List<ProcessedQuestionModel> processedQuestions);

    // 根据分类ID查询问题
    @Select("SELECT * FROM processed_question WHERE category_id = #{categoryId} ORDER BY create_date DESC")
    List<ProcessedQuestionModel> selectByCategoryId(int categoryId);

    // 查询所有处理后的问题
    @Select("SELECT * FROM processed_question ORDER BY create_date DESC")
    List<ProcessedQuestionModel> selectAll();
} 