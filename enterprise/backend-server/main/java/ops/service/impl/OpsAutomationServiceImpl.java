/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.ops.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.automation.entity.AutomationActionTypeEntity;
import com.apitable.automation.entity.AutomationServiceEntity;
import com.apitable.automation.entity.AutomationTriggerTypeEntity;
import com.apitable.automation.mapper.AutomationActionTypeMapper;
import com.apitable.automation.mapper.AutomationTriggerTypeMapper;
import com.apitable.base.enums.DatabaseException;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.automation.mapper.AutomationServiceMapper;
import com.apitable.enterprise.ops.ro.AutomationActionTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationActionTypeEditRO;
import com.apitable.enterprise.ops.ro.AutomationServiceCreateRO;
import com.apitable.enterprise.ops.ro.AutomationServiceEditRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeEditRO;
import com.apitable.enterprise.ops.service.IOpsAutomationService;
import com.apitable.shared.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import jakarta.annotation.Resource;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Product Operation System - Automation Service Implement Class.
 * </p>
 */
@Service
public class OpsAutomationServiceImpl implements IOpsAutomationService {

    @Resource
    private AutomationServiceMapper serviceMapper;

    @Resource
    private AutomationTriggerTypeMapper triggerTypeMapper;

    @Resource
    private AutomationActionTypeMapper actionTypeMapper;

    @Override
    public String createService(Long userId, AutomationServiceCreateRO ro) {
        this.checkServicePlugIfExist(ro.getSlug());
        String serviceId = StrUtil.isNotBlank(ro.getServiceId())
            ? ro.getServiceId() : IdUtil.createAutomationServiceId();
        AutomationServiceEntity entity =
            BeanUtil.copyProperties(ro, AutomationServiceEntity.class);
        entity.setId(IdWorker.getId());
        entity.setServiceId(serviceId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(serviceMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return serviceId;
    }

    @Override
    public void editService(Long userId, String serviceId, AutomationServiceEditRO ro) {
        Long id = this.getIdByServiceId(serviceId);
        AutomationServiceEntity entity =
            BeanUtil.copyProperties(ro, AutomationServiceEntity.class);
        entity.setId(id);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(serviceMapper.updateById(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
    }

    @Override
    public void deleteService(Long userId, String serviceId) {
        Long id = this.getIdByServiceId(serviceId);
        boolean flag = SqlHelper.retBool(serviceMapper.deleteById(id));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }

    public void checkServiceIfExist(String serviceId) {
        this.getIdByServiceId(serviceId);
    }

    private Long getIdByServiceId(String serviceId) {
        Long id = serviceMapper.selectIdByServiceId(serviceId);
        return Optional.ofNullable(id)
            .orElseThrow(() -> new BusinessException("Automation Service not exist."));
    }

    private void checkServicePlugIfExist(String slug) {
        Long id = serviceMapper.selectIdBySlugIncludeDeleted(slug);
        if (id != null) {
            throw new BusinessException("Slug have been existed.");
        }
    }

    @Override
    public String createTriggerType(Long userId, AutomationTriggerTypeCreateRO ro) {
        this.checkServiceIfExist(ro.getServiceId());
        String triggerTypeId = StrUtil.isNotBlank(ro.getTriggerTypeId())
            ? ro.getTriggerTypeId() : IdUtil.createAutomationTriggerTypeId();
        AutomationTriggerTypeEntity entity =
            BeanUtil.copyProperties(ro, AutomationTriggerTypeEntity.class);
        entity.setId(IdWorker.getId());
        entity.setTriggerTypeId(triggerTypeId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(triggerTypeMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return triggerTypeId;
    }

    @Override
    public void editTriggerType(Long userId, String triggerTypeId, AutomationTriggerTypeEditRO ro) {
        Long id = this.getIdByTriggerTypeId(triggerTypeId);
        AutomationTriggerTypeEntity entity =
            BeanUtil.copyProperties(ro, AutomationTriggerTypeEntity.class);
        entity.setId(id);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(triggerTypeMapper.updateById(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
    }

    @Override
    public void deleteTriggerType(Long userId, String triggerTypeId) {
        Long id = this.getIdByTriggerTypeId(triggerTypeId);
        boolean flag = SqlHelper.retBool(triggerTypeMapper.deleteById(id));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }

    private Long getIdByTriggerTypeId(String triggerTypeId) {
        Long id = triggerTypeMapper.selectIdByTriggerTypeId(triggerTypeId);
        return Optional.ofNullable(id)
            .orElseThrow(() -> new BusinessException("Trigger Type not exist."));
    }

    @Override
    public String createActionType(Long userId, AutomationActionTypeCreateRO ro) {
        this.checkServiceIfExist(ro.getServiceId());
        String actionTypeId = StrUtil.isNotBlank(ro.getActionTypeId())
            ? ro.getActionTypeId() : IdUtil.createAutomationActionTypeId();
        AutomationActionTypeEntity entity =
            BeanUtil.copyProperties(ro, AutomationActionTypeEntity.class);
        entity.setId(IdWorker.getId());
        entity.setActionTypeId(actionTypeId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(actionTypeMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return actionTypeId;
    }

    @Override
    public void editActionType(Long userId, String actionTypeId, AutomationActionTypeEditRO ro) {
        Long id = this.getIdByActionTypeId(actionTypeId);
        AutomationActionTypeEntity entity =
            BeanUtil.copyProperties(ro, AutomationActionTypeEntity.class);
        entity.setId(id);
        entity.setUpdatedBy(userId);
        boolean flag = SqlHelper.retBool(actionTypeMapper.updateById(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
    }

    @Override
    public void deleteActionType(Long userId, String actionTypeId) {
        Long id = this.getIdByActionTypeId(actionTypeId);
        boolean flag = SqlHelper.retBool(actionTypeMapper.deleteById(id));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }

    private Long getIdByActionTypeId(String actionTypeId) {
        Long id = actionTypeMapper.selectIdByActionTypeId(actionTypeId);
        return Optional.ofNullable(id)
            .orElseThrow(() -> new BusinessException("Action Type not exist."));
    }
}
