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

package com.apitable.enterprise.vikabilling.service.impl;

import java.util.List;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.mapper.SocialDingTalkOrderMapper;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkOrderService;
import com.apitable.enterprise.vikabilling.entity.SocialDingtalkOrderEntity;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocialDingTalkOrderServiceImpl extends ServiceImpl<SocialDingTalkOrderMapper,
        SocialDingtalkOrderEntity> implements ISocialDingTalkOrderService {

    @Override
    public Integer getStatusByOrderId(String tenantId, String appId, String orderId) {
        return baseMapper.selectStatusByOrderId(orderId, tenantId, appId);
    }

    @Override
    public void createOrder(SyncHttpMarketOrderEvent event) {
        SocialDingtalkOrderEntity entity = SocialDingtalkOrderEntity.builder()
                .tenantId(event.getCorpId())
                .orderId(event.getOrderId())
                .appId(event.getSuiteId())
                .itemCode(event.getItemCode())
                .orderData(JSONUtil.toJsonStr(event))
                .createdBy(-1L)
                .updatedBy(-1L)
                .build();
        baseMapper.insert(entity);
    }

    @Override
    public void updateTenantOrderStatusByOrderId(String tenantId, String appId, String orderId, Integer status) {
        baseMapper.updateStatusByTenantIdAndAppIdAndOrderId(tenantId, appId, orderId, 1);
    }

    @Override
    public List<String> getOrdersByTenantIdAndAppId(String tenantId, String appId) {
        return baseMapper.selectOrderDataByTenantIdAndAppId(tenantId, appId);
    }

    @Override
    public List<String> getOrderIdsByTenantIdAndAppIdAndItemCode(String tenantId, String appId, String itemCode) {
        return baseMapper.selectOrderIdByTenantIdAndAppIdAndItemCode(tenantId, appId, itemCode);
    }
}
