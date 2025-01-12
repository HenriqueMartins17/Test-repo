package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.DatasheetMeta;
import org.apache.ibatis.annotations.Param;

public interface DatasheetMetaMapper {

    List<DatasheetMeta> selectByDatasheetIds(@Param("datasheetIds") Collection<String> datasheetIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<DatasheetMeta> entities);
}
