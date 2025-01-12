package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.DatasheetWidget;
import org.apache.ibatis.annotations.Param;

public interface DatasheetWidgetMapper {

    List<DatasheetWidget> selectByDatasheetIds(@Param("datasheetIds") Collection<String> datasheetIds);

    int insertBatch(@Param("entities") List<DatasheetWidget> entities);
}
