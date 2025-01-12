package com.apitable.appdata.shared.asset.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.appdata.shared.asset.mapper.AssetMapper;
import com.apitable.appdata.shared.asset.pojo.Asset;
import com.apitable.appdata.shared.asset.service.IAssetService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AssetServiceImpl implements IAssetService {

    @Resource
    private AssetMapper assetMapper;

    @Override
    public List<Asset> getAssets(Collection<Long> assetIds) {
        return assetMapper.selectByIds(assetIds);
    }

    @Override
    public List<Asset> getAssetsByFileUrls(Collection<String> fileUrls) {
        if (fileUrls.isEmpty()) {
            return new ArrayList<>();
        }
        return assetMapper.selectByFileUrls(fileUrls);
    }

    @Override
    public Map<Long, Long> parseAssetAndReturnAssetIdMap(List<Asset> assets) {
        if (assets.isEmpty()) {
            return new HashMap<>();
        }
        // Get asset with the same checksum exists in the target db
        Set<String> checksums = assets.stream().map(Asset::getChecksum).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        List<Asset> existChecksumAssets = assetMapper.selectByChecksums(checksums);
        Map<String, Asset> checksumToAssetMap = existChecksumAssets.stream().collect(Collectors.toMap(Asset::getChecksum, i -> i));

        // Get asset with the same file url exists in the target db
        Set<String> fileUrls = assets.stream().map(Asset::getFileUrl).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        List<Asset> existFileUrlAssets = this.getAssetsByFileUrls(fileUrls);
        Map<String, Asset> fileUrlToAssetMap = existFileUrlAssets.stream().collect(Collectors.toMap(Asset::getFileUrl, i -> i));

        Map<Long, Long> newAssetIdMap = new HashMap<>();
        List<Asset> entities = new ArrayList<>();
        for (Asset asset : assets) {
            Asset entity = BeanUtil.copyProperties(asset, Asset.class);
            if (checksumToAssetMap.containsKey(asset.getChecksum())) {
                Asset existAsset = checksumToAssetMap.get(asset.getChecksum());
                // If asset with same checksum and file url, it doesn't need to deal with.
                if (existAsset.getFileUrl().equals(asset.getFileUrl())) {
                    newAssetIdMap.put(asset.getId(), existAsset.getId());
                    continue;
                }
                // Else, setting null value to avoid unique conflict about checksum
                entity.setChecksum(null);
                entity.setHeadSum(asset.getChecksum());
            }
            if (fileUrlToAssetMap.containsKey(asset.getFileUrl())) {
                Asset existAsset = fileUrlToAssetMap.get(asset.getFileUrl());
                if (existAsset.getChecksum() == null && Optional.ofNullable(asset.getChecksum()).orElse(asset.getHeadSum()).equals(existAsset.getHeadSum())) {
                    newAssetIdMap.put(asset.getId(), existAsset.getId());
                    continue;
                }
            }
            entity.setId(IdWorker.getId());
            entities.add(entity);
            newAssetIdMap.put(asset.getId(), entity.getId());
        }
        if (!entities.isEmpty()) {
            assetMapper.insertBatch(entities);
        }
        return newAssetIdMap;
    }
}
