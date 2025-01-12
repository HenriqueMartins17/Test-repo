package com.apitable.appdata.shared.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.base.enums.SystemConfigType;
import com.apitable.appdata.shared.base.mapper.SystemConfigMapper;
import com.apitable.appdata.shared.base.pojo.SystemConfig;
import com.apitable.appdata.shared.base.service.ISystemConfigService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.template.model.RecommendConfig;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl implements ISystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> getWizardConfigs() {
        return systemConfigMapper.selectByType(SystemConfigType.WIZARD_CONFIG.getType());
    }

    @Override
    public void parseSystemConfigData(List<SystemConfig> systemConfigs) {
        systemConfigMapper.deleteByType(SystemConfigType.WIZARD_CONFIG.getType());
        systemConfigMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, systemConfigs);
    }

    @Override
    public void parseRecommendConfigData(Map<String, String> newTemplateIdMap, List<SystemConfig> systemConfigs) {
        systemConfigMapper.deleteByType(SystemConfigType.RECOMMEND_CONFIG.getType());
        if (systemConfigs.isEmpty()) {
            return;
        }
        for (SystemConfig systemConfig : systemConfigs) {
            systemConfig.setId(IdWorker.getId());
            RecommendConfig config = JSONUtil.parseObj(systemConfig.getConfigMap()).toBean(RecommendConfig.class);
            config.getTop().stream().filter(top -> newTemplateIdMap.containsKey(top.getTemplateId()))
                    .forEach(top -> top.setTemplateId(newTemplateIdMap.get(top.getTemplateId())));
            config.getTemplateGroups().stream().filter(group -> CollUtil.containsAny(newTemplateIdMap.keySet(), group.getTemplateIds()))
                    .forEach(group -> {
                        List<String> templateIds = group.getTemplateIds().stream().map(i -> newTemplateIdMap.getOrDefault(i, i)).collect(Collectors.toList());
                        group.setTemplateIds(templateIds);
                    });
            systemConfig.setConfigMap(JSONUtil.parseObj(config).toString());
        }
        systemConfigMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, systemConfigs);
    }
}
