package com.apitable.appdata.shared.space.mapper;

import com.apitable.appdata.shared.space.pojo.Space;
import org.apache.ibatis.annotations.Param;

public interface SpaceMapper {

    int countBySpaceId(@Param("spaceId") String spaceId);

    int insert(@Param("entity") Space entity);

    int deleteBySpaceId(@Param("spaceId") String spaceId);
}
