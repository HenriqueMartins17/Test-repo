package com.apitable.appdata.shared.base.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.base.pojo.LabsFeatures;
import org.apache.ibatis.annotations.Param;

public interface LabFeatureMapper {

    void insertBatch(@Param("entities") Collection<LabsFeatures> entities);

    int remove(@Param("userId") Long userId);

    int deleteByFeatureKeyIn(@Param("featureKeys") Collection<String> featureKeys);

}
