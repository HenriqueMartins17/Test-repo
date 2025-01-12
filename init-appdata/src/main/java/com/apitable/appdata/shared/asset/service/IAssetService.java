package com.apitable.appdata.shared.asset.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.apitable.appdata.shared.asset.pojo.Asset;

public interface IAssetService {

    List<Asset> getAssets(Collection<Long> assetIds);

    List<Asset> getAssetsByFileUrls(Collection<String> fileUrls);

    Map<Long, Long> parseAssetAndReturnAssetIdMap(List<Asset> assets);
}
