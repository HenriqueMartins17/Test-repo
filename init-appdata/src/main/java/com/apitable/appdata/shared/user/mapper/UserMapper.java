package com.apitable.appdata.shared.user.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.user.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByEmail(@Param("email") String email);

    User selectByMobilePhone(@Param("mobilePhone") String mobilePhone);

    List<String> selectEmailByEmailIn(@Param("emails") Collection<String> emails);

    int insertBatch(@Param("entities") Collection<User> entities);
}
