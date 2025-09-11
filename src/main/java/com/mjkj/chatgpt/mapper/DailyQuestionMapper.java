package com.mjkj.chatgpt.mapper;

import com.mjkj.chatgpt.model.DailyQuestionModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日常问答Mapper接口
 */
@Mapper
@Repository
public interface DailyQuestionMapper {
    
    /**
     * 插入日常问答
     */
    @Insert("INSERT INTO daily_question (question, answer, category, create_time, update_time, status, sort_weight, remark) " +
            "VALUES (#{question}, #{answer}, #{category}, #{createTime}, #{updateTime}, #{status}, #{sortWeight}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(DailyQuestionModel dailyQuestion);
    
    /**
     * 根据ID更新日常问答
     */
    @Update("UPDATE daily_question SET question = #{question}, answer = #{answer}, category = #{category}, " +
            "update_time = #{updateTime}, status = #{status}, sort_weight = #{sortWeight}, remark = #{remark} " +
            "WHERE id = #{id}")
    int updateById(DailyQuestionModel dailyQuestion);
    
    /**
     * 根据ID删除日常问答
     */
    @Delete("DELETE FROM daily_question WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询日常问答
     */
    @Select("SELECT id, question, answer, category, create_time, update_time, status, sort_weight, remark " +
            "FROM daily_question WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "question", column = "question"),
        @Result(property = "answer", column = "answer"),
        @Result(property = "category", column = "category"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "sortWeight", column = "sort_weight"),
        @Result(property = "remark", column = "remark")
    })
    DailyQuestionModel selectById(@Param("id") Long id);
    
    /**
     * 查询所有启用的日常问答
     */
    @Select("SELECT id, question, answer, category, create_time, update_time, status, sort_weight, remark " +
            "FROM daily_question WHERE status = 1 ORDER BY sort_weight DESC, create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "question", column = "question"),
        @Result(property = "answer", column = "answer"),
        @Result(property = "category", column = "category"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "sortWeight", column = "sort_weight"),
        @Result(property = "remark", column = "remark")
    })
    List<DailyQuestionModel> selectAllEnabled();
    
    /**
     * 根据分类查询日常问答
     */
    @Select("SELECT id, question, answer, category, create_time, update_time, status, sort_weight, remark " +
            "FROM daily_question WHERE status = 1 AND category = #{category} " +
            "ORDER BY sort_weight DESC, create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "question", column = "question"),
        @Result(property = "answer", column = "answer"),
        @Result(property = "category", column = "category"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "sortWeight", column = "sort_weight"),
        @Result(property = "remark", column = "remark")
    })
    List<DailyQuestionModel> selectByCategory(@Param("category") String category);
    
    /**
     * 根据关键词搜索问题
     */
    @Select("SELECT id, question, answer, category, create_time, update_time, status, sort_weight, remark " +
            "FROM daily_question WHERE status = 1 " +
            "AND (question LIKE CONCAT('%', #{keyword}, '%') " +
            "OR answer LIKE CONCAT('%', #{keyword}, '%') " +
            "OR category LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY sort_weight DESC, create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "question", column = "question"),
        @Result(property = "answer", column = "answer"),
        @Result(property = "category", column = "category"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "sortWeight", column = "sort_weight"),
        @Result(property = "remark", column = "remark")
    })
    List<DailyQuestionModel> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 分页查询日常问答
     */
    @Select("SELECT id, question, answer, category, create_time, update_time, status, sort_weight, remark " +
            "FROM daily_question ORDER BY sort_weight DESC, create_time DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "question", column = "question"),
        @Result(property = "answer", column = "answer"),
        @Result(property = "category", column = "category"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "sortWeight", column = "sort_weight"),
        @Result(property = "remark", column = "remark")
    })
    List<DailyQuestionModel> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计总数
     */
    @Select("SELECT COUNT(*) FROM daily_question")
    int countTotal();
}
