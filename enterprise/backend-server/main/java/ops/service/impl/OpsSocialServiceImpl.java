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

import com.apitable.enterprise.ops.service.IOpsSocialService;
import com.apitable.enterprise.social.service.IFeishuEventService;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.IFeishuTenantContactService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.shared.holder.UserHolder;
import com.vikadata.social.feishu.model.v3.FeishuDeptObject;
import com.vikadata.social.feishu.model.v3.FeishuUserObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * Product Operation System - Social Service Implement Class.
 * </p>
 */
@Slf4j
@Service
public class OpsSocialServiceImpl implements IOpsSocialService {

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private IFeishuEventService iFeishuEventService;

    @Resource
    private IFeishuTenantContactService iFeishuTenantContactService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFeishuEvent(String tenantId) {
        UserHolder.set(-1L);
        iFeishuService.switchDefaultContext();
        MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
            iFeishuTenantContactService.fetchTenantContact(tenantId);
        String spaceId =
            iSocialTenantBindService.getTenantDepartmentBindSpaceId(iFeishuService.getIsvAppId(),
                tenantId);
        //  handle authorization change scope. synchronize departments first then employees。
        iFeishuEventService.handleTenantContactData(iFeishuService.getIsvAppId(), tenantId, spaceId,
            contactMap, null, true);
        // Handles subscription information for unbound spaces.
        iFeishuEventService.handleTenantOrders(tenantId, iFeishuService.getIsvAppId());
    }
}
