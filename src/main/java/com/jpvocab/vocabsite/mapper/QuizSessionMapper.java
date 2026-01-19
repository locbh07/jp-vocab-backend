package com.jpvocab.vocabsite.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jpvocab.vocabsite.model.QuizSession;

@Mapper
public interface QuizSessionMapper {
    int countSessionsToday(@Param("userId") Long userId,
                           @Param("today") java.sql.Date today);

    QuizSession findSessionByIndex(@Param("userId") Long userId,
                                   @Param("today") java.sql.Date today,
                                   @Param("sessionIndex") int sessionIndex);

    void insertSession(QuizSession session);
}
