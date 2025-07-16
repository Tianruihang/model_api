package com.mjkj.chatgpt.mapper;

import com.mjkj.chatgpt.model.QuestionStatisticsModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface QuestionStatisticsMapper {

    // 插入统计数据
    @Insert("INSERT INTO question_statistics (category_id, category_name, question_count, statistics_date, " +
            "create_date, update_date) VALUES (#{categoryId}, #{categoryName}, #{questionCount}, #{statisticsDate}, " +
            "#{createDate}, #{updateDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertStatistics(QuestionStatisticsModel statistics);

    // 批量插入统计数据
    @Insert("<script>" +
            "INSERT INTO question_statistics (category_id, category_name, question_count, statistics_date, " +
            "create_date, update_date) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.categoryId}, #{item.categoryName}, #{item.questionCount}, #{item.statisticsDate}, " +
            "#{item.createDate}, #{item.updateDate})" +
            "</foreach>" +
            "</script>")
    int batchInsertStatistics(List<QuestionStatisticsModel> statisticsList);

    // 查询所有统计数据
    @Select("SELECT * FROM question_statistics ORDER BY statistics_date DESC, question_count DESC")
    List<QuestionStatisticsModel> selectAllStatistics();

    // 根据日期范围查询统计数据
    @Select("SELECT * FROM question_statistics WHERE statistics_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY statistics_date DESC, question_count DESC")
    List<QuestionStatisticsModel> selectStatisticsByDateRange(@Param("startDate") Date startDate, 
                                                           @Param("endDate") Date endDate);

    // 查询最新的统计数据
    @Select("SELECT * FROM question_statistics WHERE statistics_date = (SELECT MAX(statistics_date) FROM question_statistics)")
    List<QuestionStatisticsModel> selectLatestStatistics();
} 