package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.Vocabulary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VocabularyMapper {
    @Select("SELECT * FROM vocabulary WHERE topic LIKE CONCAT(#{prefix}, '%')")
    List<Vocabulary> getWordsByPrefix(@Param("prefix") String prefix);

    @Select("SELECT DISTINCT topic FROM vocabulary WHERE topic LIKE CONCAT(#{prefix}, '%')")
    List<String> getTopicsByPrefix(@Param("prefix") String prefix);
}