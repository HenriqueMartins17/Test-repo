package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetChangesetEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数表操作变更合集表 Mapper 接口
 */
@Mapper
public interface DatasheetChangesetMapper extends BaseMapper<DatasheetChangesetEntity> {

    List<Long> selectPageById(Long id, Integer count);

    List<DatasheetChangesetEntity> selectByDatasheetIdAndMigrationId(@Param("datasheetId")String datasheetId
            , @Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetChangesetEntity> selectByMigrationId(@Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    int insertBatch(@Param("entities") List<DatasheetChangesetEntity> entities);
}
