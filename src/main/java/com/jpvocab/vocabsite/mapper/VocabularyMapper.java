package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.Vocabulary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

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

    @Select({
        "<script>",
        "SELECT * FROM vocabulary",
        "WHERE 1=1",
        "<if test='keyword != null and keyword.trim() != \"\"'>",
        "  AND (",
        "    word_ja ILIKE CONCAT('%', #{keyword}, '%')",
        "    OR word_hira_kana ILIKE CONCAT('%', #{keyword}, '%')",
        "    OR word_romaji ILIKE CONCAT('%', #{keyword}, '%')",
        "    OR word_vi ILIKE CONCAT('%', #{keyword}, '%')",
        "  )",
        "</if>",
        "<if test='topic != null and topic.trim() != \"\"'>",
        "  AND topic = #{topic}",
        "</if>",
        "<if test='level != null and level.trim() != \"\"'>",
        "  AND level = #{level}",
        "</if>",
        "ORDER BY id DESC",
        "LIMIT #{size} OFFSET #{offset}",
        "</script>"
    })
    List<Vocabulary> searchAdmin(
            @Param("keyword") String keyword,
            @Param("topic") String topic,
            @Param("level") String level,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Insert(
        "INSERT INTO vocabulary (" +
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
        ") VALUES (" +
        " #{word_ja}, " +
        " #{word_hira_kana}, " +
        " #{word_romaji}, " +
        " #{word_vi}, " +
        " #{example_ja}, " +
        " #{example_vi}, " +
        " #{topic}, " +
        " #{level}, " +
        " #{image_url}, " +
        " #{audio_url}, " +
        " #{core_order} " +
        ")"
    )
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insertVocabulary(Vocabulary vocab);

    @Update(
    "UPDATE vocabulary SET " +
    " word_ja = #{word_ja}, " +
    " word_hira_kana = #{word_hira_kana}, " +
    " word_romaji = #{word_romaji}, " +
    " word_vi = #{word_vi}, " +
    " example_ja = #{example_ja}, " +
    " example_vi = #{example_vi}, " +
    " topic = #{topic}, " +
    " level = #{level}, " +
    " image_url = #{image_url}, " +
    " audio_url = #{audio_url}, " +
    " core_order = #{core_order} " +
    "WHERE id = #{id}"
)
int updateVocabulary(Vocabulary vocab);

    @Update({
        "<script>",
        "UPDATE vocabulary",
        "<set>",
        "<if test='v.word_ja != null'>word_ja = #{v.word_ja},</if>",
        "<if test='v.word_hira_kana != null'>word_hira_kana = #{v.word_hira_kana},</if>",
        "<if test='v.word_romaji != null'>word_romaji = #{v.word_romaji},</if>",
        "<if test='v.word_vi != null'>word_vi = #{v.word_vi},</if>",
        "<if test='v.example_ja != null'>example_ja = #{v.example_ja},</if>",
        "<if test='v.example_vi != null'>example_vi = #{v.example_vi},</if>",
        "<if test='v.topic != null'>topic = #{v.topic},</if>",
        "<if test='v.level != null'>level = #{v.level},</if>",
        "<if test='v.image_url != null'>image_url = #{v.image_url},</if>",
        "<if test='v.audio_url != null'>audio_url = #{v.audio_url},</if>",
        "<if test='v.core_order != null'>core_order = #{v.core_order},</if>",
        "</set>",
        "WHERE id = #{id}",
        "</script>"
    })
    int updateVocabularyPartial(@Param("id") int id, @Param("v") Vocabulary vocab);
}
