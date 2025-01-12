package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.Node;
import org.apache.ibatis.annotations.Param;

public interface NodeMapper {

    List<String> selectAllSubNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    List<Node> selectByNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<Node> entities);

    void remove(@Param("userId") Long userId, @Param("nodeIds") Collection<String> nodeIds);

    void removeBySpaceIdAnd(@Param("spaceId") String spaceId,
        @Param("isTemplate") Boolean isTemplate);
}
