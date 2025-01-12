package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.Datasheet;
import org.apache.ibatis.annotations.Param;

public interface DatasheetMapper {

    List<Datasheet> selectByDatasheetIds(@Param("datasheetIds") Collection<String> datasheetIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<Datasheet> entities);
}
