package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetRecordCommentEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetRecordCommentMapper extends BaseMapper<DatasheetRecordCommentEntity> {

    List<DatasheetRecordCommentEntity> selectByDatasheetIdAndMigrationId(@Param("datasheetId")String datasheetId
            , @Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetRecordCommentEntity> selectByMigrationId(@Param("migrationId")Long migrationId, @Param("limitCount")int limitCount);

    List<DatasheetRecordCommentEntity> selectByIds(@Param("ids")List<Long> ids);

    int insertBatch(@Param("entities") List<DatasheetRecordCommentEntity> entities);
}
