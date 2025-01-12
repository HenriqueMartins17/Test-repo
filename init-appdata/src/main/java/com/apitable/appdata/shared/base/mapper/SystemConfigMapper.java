package com.apitable.appdata.shared.base.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.base.pojo.SystemConfig;
import org.apache.ibatis.annotations.Param;

public interface SystemConfigMapper {

    List<SystemConfig> selectByType(@Param("type") Integer type);

    void insertBatch(@Param("userId") Long userId, @Param("entities") Collection<SystemConfig> entities);

    void deleteByType(@Param("type") Integer type);
}
