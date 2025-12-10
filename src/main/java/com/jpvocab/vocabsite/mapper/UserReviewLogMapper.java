// com.jpvocab.vocabsite.mapper.UserReviewLogMapper

package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.DailyStudyRow;
import com.jpvocab.vocabsite.model.UserReviewLog;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserReviewLogMapper {

    @Insert("INSERT INTO user_review_log " +
            "(user_id, vocab_id, review_time, result, mode) " +
            "VALUES (#{userId}, #{vocabId}, #{reviewTime}, #{result}, #{mode})")
    void insertLog(UserReviewLog log);
    
    @Select("SELECT DATE(review_time) as study_date, COUNT(DISTINCT vocab_id) as words " +
            "FROM user_review_log " +
            "WHERE user_id = #{userId} " +
            "GROUP BY DATE(review_time) " +
            "ORDER BY study_date DESC " +
            "LIMIT #{limit}")
    List<DailyStudyRow> getDailyStudyStats(@Param("userId") Long userId,
                                           @Param("limit") int limit);
    
    @Select("SELECT COUNT(DISTINCT vocab_id) FROM user_review_log " +
            "WHERE user_id = #{userId} " +
            "AND mode = 'new' " +
            "AND DATE(review_time) = #{today}")
    int countTodayNewWords(@Param("userId") Long userId,
                           @Param("today") java.sql.Date today);

}
