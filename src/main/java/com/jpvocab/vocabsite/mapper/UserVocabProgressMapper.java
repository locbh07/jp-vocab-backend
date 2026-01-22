package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.UserVocabProgress;
import com.jpvocab.vocabsite.model.DailyStudyRow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserVocabProgressMapper {

    // ================== RESULT MAP — PHẢI ĐẶT TRÊN METHOD SELECT ==================
    @Select("SELECT * FROM user_vocab_progress WHERE user_id = #{userId} AND vocab_id = #{vocabId} LIMIT 1")
    @Results(id="ProgressMap", value = {
            @Result(property="id", column="id"),
            @Result(property="user_id", column="user_id"),
            @Result(property="vocab_id", column="vocab_id"),
            @Result(property="plan_id", column="plan_id"),
            @Result(property="stage", column="stage"),
            @Result(property="next_review_date", column="next_review_date"),
            @Result(property="times_reviewed", column="times_reviewed"),
            @Result(property="last_reviewed_at", column="last_reviewed_at"),
            @Result(property="last_result", column="last_result"),
            @Result(property="is_mastered", column="is_mastered"),
            @Result(property="first_seen_date", column="first_seen_date")
    })
    UserVocabProgress findProgress(@Param("userId") Long userId,
                                   @Param("vocabId") Long vocabId);
    // =============================================================================

    @Insert("INSERT INTO user_vocab_progress " +
            "(user_id, vocab_id, plan_id, stage, next_review_date, times_reviewed, is_mastered, first_seen_date) " +
            "VALUES (#{user_id}, #{vocab_id}, #{plan_id}, #{stage}, #{next_review_date}, #{times_reviewed}, #{is_mastered}, #{first_seen_date})")
    void insertProgress(UserVocabProgress progress);


    @Select("SELECT * FROM user_vocab_progress " +
            "WHERE user_id = #{userId} " +
            "AND next_review_date <= CURRENT_DATE " +
            "AND is_mastered = 0 " +
            "ORDER BY next_review_date ASC")
    @ResultMap("ProgressMap")
    List<UserVocabProgress> getDueReviews(Long userId);

    @Update("UPDATE user_vocab_progress SET " +
            "stage = #{stage}, " +
            "next_review_date = #{next_review_date}, " +
            "last_reviewed_at = #{last_reviewed_at}, " +
            "times_reviewed = #{times_reviewed}, " +
            "last_result = #{last_result}, " +
            "is_mastered = #{is_mastered} " +
            "WHERE id = #{id}")
    void updateProgress(UserVocabProgress progress);

    @Select("SELECT COUNT(*) FROM user_vocab_progress WHERE user_id = #{userId}")
    int countLearnedByUser(Long userId);

    @Select("SELECT COUNT(*) FROM user_vocab_progress WHERE user_id = #{userId} AND is_mastered = 1")
    int countMasteredByUser(Long userId);

    @Select(
            "SELECT COUNT(DISTINCT d) FROM (" +
            "  SELECT DATE(first_seen_date) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND first_seen_date IS NOT NULL " +
            "  UNION " +
            "  SELECT DATE(last_reviewed_at) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND last_reviewed_at IS NOT NULL" +
            ") t"
    )
    int countStudyDays(@Param("userId") Long userId);

    @Select(
            "SELECT MAX(d) FROM (" +
            "  SELECT DATE(first_seen_date) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND first_seen_date IS NOT NULL " +
            "  UNION " +
            "  SELECT DATE(last_reviewed_at) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND last_reviewed_at IS NOT NULL" +
            ") t"
    )
    java.sql.Date getLastStudyDate(@Param("userId") Long userId);

    @Select(
            "SELECT d AS study_date, COUNT(*) AS words FROM (" +
            "  SELECT DATE(first_seen_date) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND first_seen_date IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT DATE(last_reviewed_at) AS d FROM user_vocab_progress " +
            "  WHERE user_id = #{userId} AND last_reviewed_at IS NOT NULL" +
            ") t " +
            "GROUP BY d " +
            "ORDER BY d DESC " +
            "LIMIT #{limit}"
    )
    List<DailyStudyRow> getStudyDays(@Param("userId") Long userId,
                                     @Param("limit") int limit);
    
    // Đếm số từ mới đã được học lần đầu trong ngày (first_seen_date = hôm nay)
    @Select("SELECT COUNT(*) FROM user_vocab_progress " +
            "WHERE user_id = #{userId} " +
            "AND first_seen_date = #{today}")
    int countNewWordsToday(@Param("userId") Long userId,
                           @Param("today") java.sql.Date today);

}
