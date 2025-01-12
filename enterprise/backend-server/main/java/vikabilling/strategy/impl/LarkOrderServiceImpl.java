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

package com.apitable.enterprise.vikabilling.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantOrderService;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.model.SocialOrderContext;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.ISocialFeishuOrderService;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.strategy.AbstractSocialOrderService;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.LarkPlanConfigManager;
import com.apitable.shared.clock.ClockUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vikadata.social.feishu.enums.LarkOrderBuyType;
import com.vikadata.social.feishu.enums.PricePlanType;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;
import jakarta.annotation.Resource;
import java.time.ZoneOffset;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * lark order service
 * handle lark orders
 * </p>
 */
@Service
@Slf4j
public class LarkOrderServiceImpl extends AbstractSocialOrderService<OrderPaidEvent, Object> {

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private ISocialFeishuOrderService iSocialFeishuOrderService;

    @Resource
    private ISocialTenantOrderService iSocialTenantOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String retrieveOrderPaidEvent(OrderPaidEvent event) {
        SocialOrderContext context = buildSocialOrderContext(event);
        if (null == context) {
            return null;
        }
        // Create order
        String orderId = createOrder(context);
        // Create order metadata
        createOrderMetaData(orderId, OrderChannel.LARK, event);
        // Upgrade, Renewal, New Purchase, Renewal Upgrade, Trial
        String subscriptionId;
        if (LarkOrderBuyType.BUY.getType().equals(event.getBuyType()) &&
            null == context.getActivatedBundle()) {
            String bundleId = createBundle(context);
            subscriptionId = createSubscription(bundleId, context);
        } else if (LarkOrderBuyType.RENEW.getType().equals(event.getBuyType())) {
            subscriptionId = renewSubscription(context);
        } else {
            subscriptionId = upgradeSubscription(context);
        }
        // Create order item
        createOrderItem(orderId, subscriptionId, context);
        // Mark Feishu order has been processed
        iSocialFeishuOrderService.updateTenantOrderStatusByOrderId(event.getTenantKey(),
            event.getAppId(), event.getOrderId(), 1);
        return orderId;
    }

    @Override
    public void retrieveOrderRefundEvent(Object event) {
        // todo Feishu is currently refunding manually
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrateEvent(String spaceId) {
        // read binding information
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        List<String> eventList =
            iSocialTenantOrderService.getOrderDataByTenantIdAndAppId(bindInfo.getTenantId(),
                bindInfo.getAppId(), SocialPlatformType.FEISHU);
        eventList.forEach(i -> {
            OrderPaidEvent event = JSONUtil.toBean(i, OrderPaidEvent.class);
            Integer status = iSocialFeishuOrderService.getStatusByOrderId(bindInfo.getTenantId(),
                bindInfo.getAppId(),
                event.getOrderId());
            if (null == status) {
                iSocialFeishuOrderService.createOrder(event);
            }
            if (!SqlHelper.retBool(status)) {
                retrieveOrderPaidEvent(event);
            }
        });
    }

    @Override
    public SocialOrderContext buildSocialOrderContext(OrderPaidEvent event) {
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(event.getAppId(),
            event.getTenantKey());
        if (StrUtil.isBlank(spaceId)) {
            log.warn("Feishu Enterprise「{}」 has not received the application activation event.",
                event.getTenantKey());
            return null;
        }
        // Paid plan for order purchase
        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
        // If the price is null, then the basic version of Feishu is purchased
        Product product = ObjectUtil.isNull(price) ?
            BillingConfigManager.getCurrentFreeProduct(ProductChannel.LARK) :
            BillingConfigManager.getBillingConfig().getProducts().get(price.getProduct());
        SubscriptionPhase phase = PricePlanType.TRIAL.getType().equals(event.getPricePlanType()) ?
            SubscriptionPhase.TRIAL : SubscriptionPhase.FIXEDTERM;
        SocialOrderContext context = SocialOrderContext.builder()
            .productChannel(ProductChannel.LARK)
            .socialOrderId(event.getOrderId())
            .amount(event.getOrderPayPrice())
            .price(price).product(product)
            .spaceId(spaceId)
            .paidTime(ClockUtil.secondToLocalDateTime(Long.parseLong(event.getPayTime()),
                ZoneOffset.ofHours(8)))
            .createdTime(ClockUtil.secondToLocalDateTime(Long.parseLong(event.getCreateTime()),
                ZoneOffset.ofHours(8)))
            .phase(phase)
            .orderType(OrderType.ofName(event.getBuyType()))
            .orderChannel(OrderChannel.LARK)
            .activatedBundle(iBundleService.getActivatedBundleBySpaceId(spaceId))
            .build();
        // Feishu trial period 15 days
        if (SubscriptionPhase.TRIAL.equals(phase)) {
            // Feishu renewal fee can be tried, and the trial covers the trial
            if (null != context.getActivatedBundle() && !SubscriptionPhase.TRIAL.equals(
                context.getActivatedBundle().getBaseSubscription().getPhase())) {
                context.setServiceStopTime(
                    context.getActivatedBundle().getBundleEndDate().plusDays(15));
            } else {
                context.setServiceStopTime(context.getPaidTime().plusDays(15));
            }
        }
        // Feishu renewal is the end date of the previous order
        if (LarkOrderBuyType.RENEW.getType().equals(event.getBuyType())
            && null != context.getActivatedBundle()) {
            context.setServiceStartTime(context.getActivatedBundle().getBundleEndDate());
        }

        // Purchases during the trial period are also effective immediately
        if (LarkOrderBuyType.BUY.getType().equals(event.getBuyType())) {
            Bundle activeBundle = context.getActivatedBundle();
            if (null != activeBundle) {
                Subscription subscription = activeBundle.getBaseSubscription();
                if (null != subscription
                    && subscription.getPhase().equals(SubscriptionPhase.TRIAL)) {
                    context.setServiceStartTime(context.getPaidTime());
                }
            }
        }

        return context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SocialOrderStrategyFactory.register(SocialPlatformType.FEISHU, this);
    }
}
