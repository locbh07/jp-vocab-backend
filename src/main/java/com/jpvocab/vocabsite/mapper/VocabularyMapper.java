package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.Vocabulary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VocabularyMapper {
    @Select("SELECT * FROM vocabulary WHERE topic LIKE CONCAT(#{prefix}, '%') ORDER BY id ASC")
    List<Vocabulary> getWordsByPrefix(@Param("prefix") String prefix);

    @Select("SELECT topic FROM vocabulary WHERE topic LIKE CONCAT(#{prefix}, '%') GROUP BY topic ORDER BY MIN(id) ASC")
    List<String> getTopicsByPrefix(@Param("prefix") String prefix);
    
 // Lấy danh sách từ mới chưa học dựa trên core_order
    @Select(
        "SELECT " +
        " id, " +
        " word_ja, " +
        " word_hira_kana, " +
        " word_romaji, " +
        " word_vi, " +
        " example_ja, " +
        " example_vi, " +
        " topic, " +
        " level, " +
        " image_url, " +
        " audio_url, " +
        " core_order " +
        "FROM vocabulary " +
        "WHERE core_order IS NOT NULL " +
        "  AND id NOT IN ( " +
        "        SELECT vocab_id FROM user_vocab_progress WHERE user_id = #{userId} " +
        "  ) " +
        "ORDER BY core_order ASC " +
        "LIMIT #{limit}"
    )
    List<Vocabulary> getNewWordsForUser(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );



    // D) tổng số từ core (6727 hiện tại)
    @Select("SELECT COUNT(*) FROM vocabulary WHERE core_order IS NOT NULL")
    int countCoreWords();
    
    @Select("SELECT * FROM vocabulary WHERE id = #{id}")
    Vocabulary getById(@Param("id") long id);
}