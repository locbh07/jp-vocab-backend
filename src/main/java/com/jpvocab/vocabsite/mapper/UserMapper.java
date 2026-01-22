package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.UserAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO useraccount (username, passwordHash, fullName, email, role) " +
            "VALUES (#{username}, #{passwordHash}, #{fullName}, #{email}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(UserAccount user);

    @Select("SELECT * FROM useraccount WHERE username = #{username} LIMIT 1")
    UserAccount findByUsername(@Param("username") String username);

    @Select("SELECT * FROM useraccount WHERE id = #{id}")
    UserAccount findById(@Param("id") Long id);

    @Select({
        "<script>",
        "SELECT * FROM useraccount",
        "WHERE 1=1",
        "<if test='keyword != null and keyword.trim() != \"\"'>",
        "  AND (",
        "    username ILIKE CONCAT('%', #{keyword}, '%')",
        "    OR fullName ILIKE CONCAT('%', #{keyword}, '%')",
        "    OR email ILIKE CONCAT('%', #{keyword}, '%')",
        "  )",
        "</if>",
        "ORDER BY id DESC",
        "LIMIT #{size} OFFSET #{offset}",
        "</script>"
    })
    List<UserAccount> searchUsers(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Update({
        "<script>",
        "UPDATE useraccount",
        "<set>",
        "<if test='u.fullName != null'>fullName = #{u.fullName},</if>",
        "<if test='u.email != null'>email = #{u.email},</if>",
        "<if test='u.role != null'>role = #{u.role},</if>",
        "</set>",
        "WHERE id = #{id}",
        "</script>"
    })
    int updateUserFields(@Param("id") Long id, @Param("u") UserAccount user);
}
