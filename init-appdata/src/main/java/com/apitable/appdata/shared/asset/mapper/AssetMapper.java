package com.apitable.appdata.shared.asset.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.asset.pojo.Asset;
import org.apache.ibatis.annotations.Param;

public interface AssetMapper {

    List<Asset> selectByIds(@Param("assetIds") Collection<Long> assetIds);

    List<Asset> selectByFileUrls(@Param("fileUrls") Collection<String> fileUrls);

    List<Asset> selectByChecksums(@Param("checksums") Collection<String> checksums);

    int insertBatch(@Param("entities") List<Asset> entities);
}
