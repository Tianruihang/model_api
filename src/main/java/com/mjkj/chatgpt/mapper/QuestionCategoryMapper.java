package com.mjkj.chatgpt.mapper;

import com.mjkj.chatgpt.model.QuestionCategoryModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuestionCategoryMapper {

    // 插入分类
    @Insert("INSERT INTO question_category (category_name, category_description, keywords, create_date, update_date, enabled) " +
            "VALUES (#{categoryName}, #{categoryDescription}, #{keywords}, #{createDate}, #{updateDate}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertCategory(QuestionCategoryModel category);

    // 查询所有启用的分类
    @Select("SELECT * FROM question_category WHERE enabled = true ORDER BY id")
    List<QuestionCategoryModel> selectAllEnabledCategories();

    // 根据ID查询分类
    @Select("SELECT * FROM question_category WHERE id = #{id}")
    QuestionCategoryModel selectById(int id);

    // 更新分类
    @Update("UPDATE question_category SET category_name = #{categoryName}, category_description = #{categoryDescription}, " +
            "keywords = #{keywords}, update_date = #{updateDate}, enabled = #{enabled} WHERE id = #{id}")
    int updateCategory(QuestionCategoryModel category);
} 