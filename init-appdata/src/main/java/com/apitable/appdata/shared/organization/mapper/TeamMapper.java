package com.apitable.appdata.shared.organization.mapper;

import com.apitable.appdata.shared.organization.pojo.Team;
import org.apache.ibatis.annotations.Param;

public interface TeamMapper {

    int insert(@Param("entity") Team entity);

    int deleteBySpaceId(@Param("spaceId") String spaceId);
}
