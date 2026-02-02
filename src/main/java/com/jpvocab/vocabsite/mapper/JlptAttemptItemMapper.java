package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.JlptAttemptItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JlptAttemptItemMapper {
    int insertItems(@Param("items") List<JlptAttemptItem> items);

    @Select("SELECT id, attempt_id, part, section_index, question_index, question_id, " +
            "selected, correct_answer, is_correct, question_json " +
            "FROM jlpt_attempt_item WHERE attempt_id = #{attemptId} ORDER BY part, section_index, question_index")
    List<JlptAttemptItem> getItemsByAttempt(@Param("attemptId") Long attemptId);
}
