package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.JlptExam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JlptExamMapper {

    @Select("SELECT DISTINCT exam_id FROM jlpt_exam WHERE level = #{level} ORDER BY exam_id DESC")
    List<String> getExamIdsByLevel(@Param("level") String level);

    @Select("SELECT id, level, exam_id, part, source_file, json_data FROM jlpt_exam " +
            "WHERE level = #{level} AND exam_id = #{examId} ORDER BY part ASC")
    List<JlptExam> getExamParts(@Param("level") String level, @Param("examId") String examId);

    @Select("SELECT id, level, exam_id, part, source_file, json_data FROM jlpt_exam " +
            "WHERE level = #{level} AND exam_id = #{examId} AND part = #{part}")
    JlptExam getExamPart(@Param("level") String level, @Param("examId") String examId, @Param("part") int part);

    @Update("UPDATE jlpt_exam SET json_data = #{json}::jsonb WHERE level = #{level} AND exam_id = #{examId} AND part = #{part}")
    int updateExamPart(@Param("level") String level, @Param("examId") String examId, @Param("part") int part, @Param("json") String json);
}
