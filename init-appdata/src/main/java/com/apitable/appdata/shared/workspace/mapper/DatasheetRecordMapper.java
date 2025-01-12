package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.DatasheetRecord;
import org.apache.ibatis.annotations.Param;

public interface DatasheetRecordMapper {

    List<DatasheetRecord> selectByDatasheetIds(@Param("datasheetIds") Collection<String> datasheetIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<DatasheetRecord> entities);
}
