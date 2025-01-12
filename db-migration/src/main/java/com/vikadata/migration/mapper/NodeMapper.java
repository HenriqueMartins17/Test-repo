package com.vikadata.migration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vikadata.migration.entity.NodeEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NodeMapper extends BaseMapper<NodeEntity> {

    List<String> selectBySpaceId(@Param("spaceId")String spaceId, @Param("isTemplate") int isTemplate);

    List<NodeEntity> selectByNodeIds(@Param("nodeIds") List<String> nodeIds);


}
