package com.apitable.appdata.shared.organization.mapper;

import com.apitable.appdata.shared.organization.pojo.TeamMemberRel;
import org.apache.ibatis.annotations.Param;

public interface TeamMemberRelMapper {

    int insert(@Param("entity") TeamMemberRel entity);
}
