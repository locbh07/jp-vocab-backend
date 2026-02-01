// src/main/java/com/jpvocab/vocabsite/mapper/UserLearningPlanMapper.java
package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.UserLearningPlan;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserLearningPlanMapper {

    @Select("SELECT * FROM user_learning_plan " +
            "WHERE user_id = #{userId} AND is_active = 1 " +
            "ORDER BY id DESC LIMIT 1")
    UserLearningPlan getActivePlan(@Param("userId") Long userId);

    @Insert("INSERT INTO user_learning_plan " +
            "(user_id, total_words, target_months, topic_prefix, start_date, target_date, daily_new_words, is_active) " +
            "VALUES (#{user_id}, #{total_words}, #{target_months}, #{topic_prefix}, #{start_date}, #{target_date}, #{daily_new_words}, #{is_active})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void createPlan(UserLearningPlan plan);

    @Update("UPDATE user_learning_plan SET is_active = 0 " +
            "WHERE user_id = #{userId} AND is_active = 1")
    void deactivateAllPlans(@Param("userId") Long userId);
}
