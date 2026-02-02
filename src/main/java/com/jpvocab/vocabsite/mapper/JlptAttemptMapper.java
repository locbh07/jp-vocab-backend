package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.JlptAttempt;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface JlptAttemptMapper {

    @Insert("INSERT INTO jlpt_attempt (user_id, level, exam_id, started_at, finished_at, duration_sec, " +
            "score_total, score_sec1, score_sec2, score_sec3) " +
            "VALUES (#{user_id}, #{level}, #{exam_id}, #{started_at}, #{finished_at}, #{duration_sec}, " +
            "#{score_total}, #{score_sec1}, #{score_sec2}, #{score_sec3})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAttempt(JlptAttempt attempt);

    @Select("SELECT id, user_id, level, exam_id, started_at, finished_at, duration_sec, " +
            "score_total, score_sec1, score_sec2, score_sec3 " +
            "FROM jlpt_attempt WHERE user_id = #{userId} ORDER BY started_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<JlptAttempt> getAttemptsByUser(@Param("userId") Long userId,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    @Select("SELECT id, user_id, level, exam_id, started_at, finished_at, duration_sec, " +
            "score_total, score_sec1, score_sec2, score_sec3 " +
            "FROM jlpt_attempt WHERE id = #{attemptId}")
    JlptAttempt getAttemptById(@Param("attemptId") Long attemptId);
}
