package com.apitable.appdata.shared.automation.service.impl;

import com.apitable.appdata.shared.automation.mapper.AutomationActionTypeMapper;
import com.apitable.appdata.shared.automation.mapper.AutomationServiceMapper;
import com.apitable.appdata.shared.automation.mapper.AutomationTriggerTypeMapper;
import com.apitable.appdata.shared.automation.model.AutomationDataPack;
import com.apitable.appdata.shared.automation.pojo.AutomationActionType;
import com.apitable.appdata.shared.automation.pojo.AutomationService;
import com.apitable.appdata.shared.automation.pojo.AutomationTriggerType;
import com.apitable.appdata.shared.automation.service.IAutomationService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AutomationServiceImpl implements IAutomationService {

    @Resource
    private AutomationServiceMapper serviceMapper;

    @Resource
    private AutomationTriggerTypeMapper triggerTypeMapper;

    @Resource
    private AutomationActionTypeMapper actionTypeMapper;

    @Override
    public void parseAutomationDataPack(AutomationDataPack dataPack) {
        this.parseAutomationService(dataPack.getServices());
        this.parseAutomationTriggerType(dataPack.getTriggerTypes());
        this.parseAutomationActionType(dataPack.getActionTypes());
    }

    private void parseAutomationService(List<AutomationService> services) {
        if (services.isEmpty()) {
            serviceMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);
            return;
        }
        // Delete record having duplicated slug, and remove leftover
        List<String> slugs = services.stream().map(AutomationService::getSlug).collect(Collectors.toList());
        serviceMapper.deleteBySlugIn(slugs);
        serviceMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);

        services.forEach(i -> {
            i.setId(IdWorker.getId());
            i.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
            i.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        });
        serviceMapper.insertBatch(services);
    }

    private void parseAutomationTriggerType(List<AutomationTriggerType> triggerTypes) {
        if (triggerTypes.isEmpty()) {
            triggerTypeMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);
            return;
        }
        // Delete record having duplicated slug, and remove leftover
        List<String> triggerTypeIds = triggerTypes.stream().map(AutomationTriggerType::getTriggerTypeId).collect(Collectors.toList());
        triggerTypeMapper.deleteByTriggerTypeIdIn(triggerTypeIds);
        triggerTypeMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);

        triggerTypes.forEach(i -> {
            i.setId(IdWorker.getId());
            i.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
            i.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        });
        triggerTypeMapper.insertBatch(triggerTypes);
    }

    private void parseAutomationActionType(List<AutomationActionType> actionTypes) {
        if (actionTypes.isEmpty()) {
            actionTypeMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);
            return;
        }
        // Delete record having duplicated slug, and remove leftover
        List<String> triggerTypeIds = actionTypes.stream().map(AutomationActionType::getActionTypeId).collect(Collectors.toList());
        actionTypeMapper.deleteByActionTypeIdIn(triggerTypeIds);
        actionTypeMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID);

        actionTypes.forEach(i -> {
            i.setId(IdWorker.getId());
            i.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
            i.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        });
        actionTypeMapper.insertBatch(actionTypes);
    }

}
