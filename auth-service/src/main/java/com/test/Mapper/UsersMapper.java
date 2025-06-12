package com.test.Mapper;

import com.test.Pojo.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UsersMapper {

    @Select("SELECT * FROM users")
    Users findAll();

    @Select("SELECT * FROM users WHERE eemail = #{email}")
    Users findByEmail(String email);

    @Insert("INSERT INTO users(username, eemail, password, role) VALUES(#{username}, #{eemail}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Users users);
}
