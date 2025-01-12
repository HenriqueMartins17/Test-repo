package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetRecordEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetRecordMapper extends BaseMapper<DatasheetRecordEntity> {

    List<DatasheetRecordEntity> selectByDatasheetIdAndMigrationId(@Param("datasheetId")String datasheetId
            , @Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetRecordEntity> selectByMigrationId(@Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetRecordEntity> selectByIds(@Param("ids")List<Long> ids);

    int insertBatch(@Param("entities") List<DatasheetRecordEntity> entities);

}
