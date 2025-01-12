package com.apitable.appdata.shared.workspace.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.ResourceMeta;
import org.apache.ibatis.annotations.Param;

public interface ResourceMetaMapper {

    List<ResourceMeta> selectByResourceIds(@Param("resourceIds") Collection<String> resourceIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<ResourceMeta> entities);
}
