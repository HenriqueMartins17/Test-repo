package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.NodeRel;
import org.apache.ibatis.annotations.Param;

public interface NodeRelMapper {

    List<NodeRel> selectByMainNodeIds(@Param("mainNodeIds") Collection<String> mainNodeIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<NodeRel> entities);
}
