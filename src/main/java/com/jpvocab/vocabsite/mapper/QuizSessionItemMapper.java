package com.jpvocab.vocabsite.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jpvocab.vocabsite.model.QuizSessionItem;
import com.jpvocab.vocabsite.model.QuizWordDto;

@Mapper
public interface QuizSessionItemMapper {
    void insertItems(@Param("items") List<QuizSessionItem> items);

    List<QuizWordDto> getItemsBySessionId(@Param("sessionId") Long sessionId);

    List<QuizWordDto> getNextQuizItems(@Param("userId") Long userId,
                                       @Param("today") java.sql.Date today,
                                       @Param("limit") int limit);
}
