package com.apitable.appdata.shared.organization.mapper;

import com.apitable.appdata.shared.organization.pojo.Member;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {

    int insert(@Param("entity") Member entity);

    int deleteBySpaceId(@Param("spaceId") String spaceId);
}
