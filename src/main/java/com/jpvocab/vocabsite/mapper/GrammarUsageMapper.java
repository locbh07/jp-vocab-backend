package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.GrammarUsage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GrammarUsageMapper {

    @Select("SELECT * FROM grammar_usage WHERE grammar_id = #{grammarId}")
    List<GrammarUsage> getUsagesByGrammarId(@Param("grammarId") Long  grammarId);
}
