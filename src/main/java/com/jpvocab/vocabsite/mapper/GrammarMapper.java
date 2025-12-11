package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.Grammar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GrammarMapper {
    @Select("SELECT * FROM grammar WHERE level = #{level}")
    List<Grammar> getAllGrammar(@Param("level") String level);

    @Select("SELECT * FROM grammar WHERE grammar_id = #{grammarId}")
    Grammar getGrammarById(@Param("grammarId") Long  grammarId);
}
