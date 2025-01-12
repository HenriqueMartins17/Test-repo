package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetMapper extends BaseMapper<DatasheetEntity> {

    List<DatasheetEntity> selectBySpaceId(@Param("spaceId")String spaceId);

    List<DatasheetEntity> selectByIds(@Param("ids")List<Long> ids);

    List<DatasheetEntity> selectBySpaceIdAndMigrationId(@Param("spaceId")String spaceId
            , @Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetEntity> selectByMigrationId(@Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetEntity> selectByDatasheetIds(@Param("datasheetIds")List<String> datasheetIds);

    int insertBatch(@Param("entities") List<DatasheetEntity> entities);
}
