package com.apitable.appdata.shared.space.service.impl;

import com.apitable.appdata.shared.space.mapper.SpaceAssetMapper;
import com.apitable.appdata.shared.space.pojo.SpaceAsset;
import com.apitable.appdata.shared.space.service.ISpaceAssetService;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SpaceAssetServiceImpl implements ISpaceAssetService {

    @Resource
    private SpaceAssetMapper spaceAssetMapper;

    @Override
    public List<SpaceAsset> getSpaceAsset(Collection<String> nodeIds) {
        return spaceAssetMapper.selectByNodeIds(nodeIds);
    }

    @Override
    public void parseSpaceAsset(String targetSpaceId, Map<Long, Long> newAssetIdMap, List<SpaceAsset> spaceAssets) {
        if (spaceAssets.isEmpty()) {
            return;
        }
        for (SpaceAsset spaceAsset : spaceAssets) {
            spaceAsset.setSpaceId(targetSpaceId);
            if (newAssetIdMap.containsKey(spaceAsset.getAssetId())) {
                spaceAsset.setAssetId(newAssetIdMap.get(spaceAsset.getAssetId()));
            }
        }
        spaceAssetMapper.insertBatch(spaceAssets);
    }
}
