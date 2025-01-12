package com.apitable.appdata.shared.base.service;

import java.util.List;
import java.util.Map;

import com.apitable.appdata.shared.base.pojo.SystemConfig;

public interface ISystemConfigService {

    List<SystemConfig> getWizardConfigs();

    void parseSystemConfigData(List<SystemConfig> systemConfigs);

    void parseRecommendConfigData(Map<String, String> newTemplateIdMap, List<SystemConfig> systemConfigs);
}
