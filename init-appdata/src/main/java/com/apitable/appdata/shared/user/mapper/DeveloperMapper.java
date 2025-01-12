package com.apitable.appdata.shared.user.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.user.pojo.Developer;
import org.apache.ibatis.annotations.Param;

public interface DeveloperMapper {

    int insertBatch(@Param("entities") Collection<Developer> entities);
}
