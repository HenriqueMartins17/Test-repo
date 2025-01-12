package com.apitable.appdata.shared.space.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.space.pojo.SpaceAsset;
import org.apache.ibatis.annotations.Param;

public interface SpaceAssetMapper {

    List<SpaceAsset> selectByNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    int insertBatch(@Param("entities") List<SpaceAsset> entities);
}
