package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.NodeDesc;
import org.apache.ibatis.annotations.Param;

public interface NodeDescMapper {

    List<NodeDesc> selectByNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    int insertBatch(@Param("entities") List<NodeDesc> entities);
}
