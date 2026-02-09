package com.jpvocab.vocabsite.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jpvocab.vocabsite.model.LearningWordDto;

@Mapper
public interface LearningTodayMapper {
    List<LearningWordDto> getTodayLearnedWords(@Param("userId") Long userId,
                                               @Param("startDate") java.sql.Date startDate,
                                               @Param("endDate") java.sql.Date endDate);
}
