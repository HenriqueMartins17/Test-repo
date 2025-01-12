package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetRecordSourceEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetRecordSourceMapper extends BaseMapper<DatasheetRecordSourceEntity> {

    List<DatasheetRecordSourceEntity> selectByDatasheetIdAndRecordIds(@Param("datasheetId")String datasheetId
            , @Param("list")List<String> recordIds);

    List<DatasheetRecordSourceEntity> selectByDatasheetIdsAndRecordIds(@Param("datasheetIds")List<String> datasheetIds
            , @Param("list")List<String> recordIds);

}
