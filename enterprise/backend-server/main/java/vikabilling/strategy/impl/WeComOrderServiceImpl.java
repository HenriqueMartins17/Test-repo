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

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.model.SocialOrderContext;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISocialWecomOrderService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.strategy.AbstractSocialOrderService;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.WeComPlanConfigManager;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.shared.clock.ClockUtil;
import com.vikadata.social.wecom.event.order.WeComOrderPaidEvent;
import com.vikadata.social.wecom.event.order.WeComOrderRefundEvent;
import com.vikadata.social.wecom.model.WxCpIsvAuthInfo.EditionInfo.Agent;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation for wecom isv order event.
 * </p>
 */
@Slf4j
@Service
public class WeComOrderServiceImpl
    extends AbstractSocialOrderService<WeComOrderPaidEvent, WeComOrderRefundEvent> {
    @Resource
    private IBundleService bundleService;

    @Resource
    private IOrderItemService orderItemService;

    @Resource
    private IOrderV2Service orderV2Service;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Resource
    private ISocialWecomOrderService iSocialWecomOrderService;

    @Resource
    private ISubscriptionService subscriptionService;

    @Resource
    private ISocialCpIsvService iSocialCpIsvService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String retrieveOrderPaidEvent(WeComOrderPaidEvent event) {
        SocialOrderContext context = buildSocialOrderContext(event);
        if (Objects.isNull(context)) {
            return null;
        }
        // Handle subscription
        String subscriptionId;
        Bundle activeBundle = context.getActivatedBundle();
        if (Objects.isNull(activeBundle) && (context.getOrderType() == OrderType.BUY
            || context.getOrderType() == OrderType.RENEW)) {
            // 3.1 Crate bundle as first subscription
            String bundleId = createBundle(context);
            subscriptionId = createSubscription(bundleId, context);
        } else if (Objects.nonNull(activeBundle)
            && SubscriptionPhase.TRIAL.equals(context.getPhase())) {
            // 3.2 Upgrade while last subscription is trial
            subscriptionId = upgradeSubscription(context);
        } else {
            // 3.3 Renew bundle end time, and create a new subscription
            // Both upgrade and renewal in wecom should create new order, tenant will go back to last subscription while refund.
            // So we need to create a new subscription if bundle existed
            // remove last subscription for effective immediately
            subscriptionService.removeBatchBySubscriptionIds(
                Collections.singletonList(activeBundle.getBaseSubscription().getSubscriptionId()));
            subscriptionId = renewSubscription(context);
        }
        // cp which on trial have not test order, just create space subscription info
        if (!SubscriptionPhase.TRIAL.equals(context.getPhase())) {
            iSocialWecomOrderService.createOrder(event);
            // 1 Save billing order
            String orderId = createOrder(context);
            // 2 Save billing order meta
            createOrderMetaData(orderId, OrderChannel.WECOM, event);
            // 4 Save order item
            createOrderItem(orderId, subscriptionId, context);
            SpringContextHolder.getApplicationContext()
                .publishEvent(new SyncOrderEvent(this, orderId));
            return orderId;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retrieveOrderRefundEvent(WeComOrderRefundEvent event) {
        // 1 Retrieve space ID of tenant
        String spaceId = socialTenantBindService.getTenantDepartmentBindSpaceId(event.getSuiteId(),
            event.getPaidCorpId());
        if (CharSequenceUtil.isBlank(spaceId)) {
            log.error("Failed to handle, as this tenant haven't bind a space: {}",
                event.getPaidCorpId());
            return;
        }
        // restore the lasted subscription with un refunded
        List<String> lastUnRefundSubscriptionIds =
            iSocialWecomOrderService.getUnRefundedLastSubscriptionIds(spaceId,
                event.getSuiteId(), event.getPaidCorpId());
        if (!lastUnRefundSubscriptionIds.isEmpty()) {
            subscriptionService.restoreBySubscriptionIds(lastUnRefundSubscriptionIds);
        }
        // retrieve vika order ID related to the wecom order
        String orderId = orderV2Service.getOrderIdByChannelOrderId(spaceId, event.getOrderId());
        List<String> subscriptionIds = orderItemService.getSubscriptionIdsByOrderId(orderId);
        List<String> bundleIds = new ArrayList<>();
        if (!subscriptionIds.isEmpty()) {
            bundleIds = subscriptionService.getBundleIdsBySubscriptionIds(subscriptionIds);
            // Remove subscription if needed
            subscriptionService.removeBatchBySubscriptionIds(subscriptionIds);
        }
        // remove bundle if it's all subscriptions was deleted
        if (!bundleIds.isEmpty() && !subscriptionService.bundlesHaveSubscriptions(bundleIds)) {
            bundleService.removeBatchByBundleIds(bundleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrateEvent(String spaceId) {
        // get tenant info
        SocialTenantBindEntity bindInfo = socialTenantBindService.getBySpaceId(spaceId);
        if (null == bindInfo) {
            log.warn("wecom space not bind");
            return;
        }
        // just in trail
        Agent agent =
            iSocialCpIsvService.getCorpEditionInfo(bindInfo.getTenantId(), bindInfo.getAppId());
        // app stopped
        if (null == agent) {
            return;
        }
        if (WeComPlanConfigManager.isWeComTrialEdition(agent.getEditionId())) {
            WeComOrderPaidEvent event =
                SocialFactory.formatWecomTailEditionOrderPaidEvent(bindInfo.getAppId(),
                    bindInfo.getTenantId(), bindInfo.getCreatedAt(), agent);
            retrieveOrderPaidEvent(event);
        }
    }

    @Override
    public SocialOrderContext buildSocialOrderContext(WeComOrderPaidEvent event) {
        String spaceId = socialTenantBindService
            .getTenantDepartmentBindSpaceId(event.getSuiteId(), event.getPaidCorpId());
        if (StrUtil.isBlank(spaceId)) {
            log.error("cp not bind space：{}", event.getPaidCorpId());
            return null;
        }
        // Paid plan for order purchase
        Price price = WeComPlanConfigManager.getPriceByWeComEditionIdAndMonth(event.getEditionId(),
            event.getUserCount(), SocialFactory.getWeComOrderMonth(event.getOrderPeriod()));
        if (Objects.isNull(price)) {
            throw new BusinessException("cp cannot find price：" + JSONUtil.toJsonStr(event));
        }
        Product product =
            BillingConfigManager.getBillingConfig().getProducts().get(price.getProduct());
        SocialOrderContext orderContext = SocialOrderContext.builder()
            .productChannel(ProductChannel.WECOM)
            .socialOrderId(event.getOrderId())
            .amount(event.getPrice().longValue())
            .price(price)
            .orderChannel(OrderChannel.WECOM)
            .product(product)
            .spaceId(spaceId)
            .orderType(SocialFactory.getOrderTypeFromWeCom(event.getOrderType()))
            .paidTime(ClockUtil.secondToLocalDateTime(event.getPaidTime(), ZoneOffset.ofHours(8)))
            .createdTime(
                ClockUtil.secondToLocalDateTime(event.getOrderTime(), ZoneOffset.ofHours(8)))
            .phase(WeComPlanConfigManager.getSubscriptionPhase(event.getEditionId()))
            .serviceStartTime(
                ClockUtil.secondToLocalDateTime(event.getBeginTime(), ZoneOffset.ofHours(8)))
            .serviceStopTime(
                ClockUtil.secondToLocalDateTime(event.getEndTime(), ZoneOffset.ofHours(8)))
            .build();
        orderContext.setActivatedBundle(bundleService.getActivatedBundleBySpaceId(spaceId));
        // enterprise WeChat renewal is the start date of the previous order
        if (OrderType.RENEW.equals(orderContext.getOrderType())
            && null != orderContext.getActivatedBundle()) {
            orderContext.setServiceStartTime(
                orderContext.getActivatedBundle().getBundleStartDate());
        }
        return orderContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SocialOrderStrategyFactory.register(SocialPlatformType.WECOM, this);
    }

}
