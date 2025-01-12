package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetMetaEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetMetaMapper extends BaseMapper<DatasheetMetaEntity> {

    List<DatasheetMetaEntity> selectByDatasheetIds(@Param("list")List<String> datasheetIds);

    List<DatasheetMetaEntity> selectByDatasheetIdAndMigrationId(@Param("datasheetId")String datasheetId
            , @Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetMetaEntity> selectByMigrationId(@Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetMetaEntity> selectByIds(@Param("ids")List<Long> ids);

    int insertBatch(@Param("entities") List<DatasheetMetaEntity> entities);


}
