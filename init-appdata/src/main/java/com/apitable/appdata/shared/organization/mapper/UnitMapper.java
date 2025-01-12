package com.apitable.appdata.shared.organization.mapper;

import com.apitable.appdata.shared.organization.pojo.Unit;
import org.apache.ibatis.annotations.Param;

public interface UnitMapper {

    int insert(@Param("entity") Unit entity);
}
