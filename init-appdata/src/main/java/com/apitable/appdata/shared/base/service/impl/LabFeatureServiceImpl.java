package com.apitable.appdata.shared.base.service.impl;

import com.apitable.appdata.shared.base.mapper.LabFeatureMapper;
import com.apitable.appdata.shared.base.pojo.LabsFeatures;
import com.apitable.appdata.shared.base.service.ILabFeatureService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LabFeatureServiceImpl implements ILabFeatureService {

    @Resource
    private LabFeatureMapper labFeatureMapper;

    @Override
    public void parseLabFeatureData(List<LabsFeatures> labsFeatures) {
        if (labsFeatures.isEmpty()) {
            labFeatureMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);
            return;
        }
        // Delete record having duplicated slug, and remove leftover
        List<String> featureKeys = labsFeatures.stream().map(LabsFeatures::getFeatureKey).collect(Collectors.toList());
        labFeatureMapper.deleteByFeatureKeyIn(featureKeys);
        labFeatureMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);

        labsFeatures.forEach(i -> i.setId(IdWorker.getId()));
        labFeatureMapper.insertBatch(labsFeatures);
    }
}
