package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.DatasheetChangesetSourceEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasheetChangesetSourceMapper extends BaseMapper<DatasheetChangesetSourceEntity> {

    List<DatasheetChangesetSourceEntity> selectByDatasheetIdAndMessageIds(@Param("datasheetId")String datasheetId
            , @Param("list")List<String> messageIds);

    List<DatasheetChangesetSourceEntity> selectByDatasheetIdsAndMessageIds(@Param("datasheetIds")List<String> datasheetIds
            , @Param("list")List<String> messageIds);

}
