package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.JlptExamRevision;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface JlptExamRevisionMapper {

    @Insert("INSERT INTO jlpt_exam_revision (level, exam_id, part, editor_id, note, json_data) " +
            "VALUES (#{level}, #{exam_id}, #{part}, #{editor_id}, #{note}, #{json_data}::jsonb)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRevision(JlptExamRevision revision);

    @Select("SELECT id, level, exam_id, part, editor_id, note, json_data, created_at " +
            "FROM jlpt_exam_revision WHERE level = #{level} AND exam_id = #{examId} AND part = #{part} " +
            "ORDER BY created_at DESC")
    List<JlptExamRevision> getRevisions(@Param("level") String level,
                                        @Param("examId") String examId,
                                        @Param("part") int part);

    @Select("SELECT id, level, exam_id, part, editor_id, note, json_data, created_at " +
            "FROM jlpt_exam_revision WHERE id = #{id}")
    JlptExamRevision getById(@Param("id") Long id);
}
