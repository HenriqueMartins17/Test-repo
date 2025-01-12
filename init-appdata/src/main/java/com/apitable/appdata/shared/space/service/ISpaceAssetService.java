package com.apitable.appdata.shared.space.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.apitable.appdata.shared.space.pojo.SpaceAsset;

public interface ISpaceAssetService {

    List<SpaceAsset> getSpaceAsset(Collection<String> nodeIds);

    void parseSpaceAsset(String targetSpaceId, Map<Long, Long> newAssetIdMap, List<SpaceAsset> spaceAssets);
}
