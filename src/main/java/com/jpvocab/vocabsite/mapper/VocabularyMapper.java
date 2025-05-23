package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.Vocabulary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VocabularyMapper {

	@Select("SELECT * FROM vocabulary")
	List<Vocabulary> findAll();
	
	@Select("SELECT DISTINCT topic FROM vocabulary")
    List<String> findDistinctTopics();
	
	@Select("SELECT * FROM vocabulary WHERE topic = #{topic}")
    List<Vocabulary> findByTopic(String topic);

}