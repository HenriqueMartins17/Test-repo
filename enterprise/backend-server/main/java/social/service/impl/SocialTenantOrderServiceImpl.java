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

package com.apitable.enterprise.social.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialTenantOrderMapper;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.service.ISocialTenantOrderService;
import com.apitable.enterprise.social.entity.SocialTenantOrderEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Third party platform integration - enterprise tenant order service interface implementation
 * </p>
 */
@Service
@Slf4j
public class SocialTenantOrderServiceImpl
    extends ServiceImpl<SocialTenantOrderMapper, SocialTenantOrderEntity>
    implements ISocialTenantOrderService {

    @Resource
    private SocialTenantOrderMapper socialTenantOrderMapper;


    @Override
    public Boolean tenantOrderExisted(String channelOrderId, String tenantId, String appId,
                                      SocialPlatformType platformType) {
        return SqlHelper.retBool(
            socialTenantOrderMapper.selectCountByChannelOrderId(channelOrderId, tenantId, appId,
                platformType));
    }

    @Override
    public Boolean createTenantOrder(String orderId, String tenantId, String appId,
                                     SocialPlatformType platformType, String orderData) {
        SocialTenantOrderEntity entity = new SocialTenantOrderEntity();
        entity.setChannelOrderId(orderId);
        entity.setTenantId(tenantId);
        entity.setAppId(appId);
        entity.setPlatform(platformType.getValue());
        entity.setOrderData(orderData);
        return SqlHelper.retBool(socialTenantOrderMapper.insert(entity));
    }

    @Override
    public List<String> getOrderDataByTenantIdAndAppId(String tenantId, String appId,
                                                       SocialPlatformType platformType) {
        return socialTenantOrderMapper.selectOrderDataByTenantIdAndAppId(tenantId, appId,
            platformType);
    }
}
