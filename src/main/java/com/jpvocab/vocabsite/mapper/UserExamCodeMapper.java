package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.UserExamCode;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserExamCodeMapper {

    @Select("SELECT id, user_id, level, code, enabled, updated_at FROM user_exam_code WHERE user_id = #{userId} ORDER BY level ASC")
    List<UserExamCode> getByUser(@Param("userId") Long userId);

    @Select("SELECT level FROM user_exam_code WHERE user_id = #{userId} AND enabled = true AND code = #{code} ORDER BY level ASC")
    List<String> getAllowedLevels(@Param("userId") Long userId, @Param("code") String code);

    @Insert({
        "<script>",
        "INSERT INTO user_exam_code (user_id, level, code, enabled, updated_at) VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(",
        "#{item.user_id},",
        "#{item.level},",
        "#{item.code},",
        "#{item.enabled},",
        "NOW()",
        ")",
        "</foreach>",
        "ON CONFLICT (user_id, level) DO UPDATE SET",
        "code = EXCLUDED.code,",
        "enabled = EXCLUDED.enabled,",
        "updated_at = NOW()",
        "</script>"
    })
    int upsertCodes(@Param("items") List<UserExamCode> items);
}
