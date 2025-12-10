package com.jpvocab.vocabsite.mapper;

import com.jpvocab.vocabsite.model.UserAccount;
import org.apache.ibatis.annotations.*;

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
}
