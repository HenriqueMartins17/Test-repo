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

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.model.SocialOrderContext;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkOrderService;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkRefundService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.strategy.AbstractSocialOrderService;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.DingTalkPlanConfigManager;
import com.apitable.enterprise.grpc.CorpBizDataDto;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.shared.clock.ClockUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vikadata.social.dingtalk.enums.DingTalkBizType;
import com.vikadata.social.dingtalk.enums.DingTalkOrderChargeType;
import com.vikadata.social.dingtalk.enums.DingTalkOrderType;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * ding talk orders service implements.
 * handle ding talk order event.
 * </p>
 */
@Service
@Slf4j
public class DingTalkOrderServiceImpl
    extends AbstractSocialOrderService<SyncHttpMarketOrderEvent, SyncHttpMarketServiceCloseEvent> {
    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialDingTalkOrderService iSocialDingTalkOrderService;

    @Resource
    private ISocialDingTalkRefundService iSocialDingTalkRefundService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private IOrderV2Service iOrderV2Service;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Override
    public String retrieveOrderPaidEvent(SyncHttpMarketOrderEvent event) {
        SocialOrderContext context = buildSocialOrderContext(event);
        if (null == context) {
            return null;
        }
        // Create order
        String orderId = createOrder(context);
        // Create order metadata
        createOrderMetaData(orderId, OrderChannel.DINGTALK, event);
        // Upgrade, Renewal, New Purchase, Renewal Upgrade, Trial
        String subscriptionId;
        if (OrderType.BUY.equals(context.getOrderType()) && null == context.getActivatedBundle()) {
            // Create subscription bundle
            String bundleId = createBundle(context);
            subscriptionId = createSubscription(bundleId, context);
        } else if (OrderType.RENEW.equals(context.getOrderType())) {
            subscriptionId = renewSubscription(context);
        } else {
            subscriptionId = upgradeSubscription(context);
        }
        // Create order item
        createOrderItem(orderId, subscriptionId, context);
        // Mark the order has been processed
        iSocialDingTalkOrderService.updateTenantOrderStatusByOrderId(event.getCorpId(),
            event.getSuiteId(),
            event.getOrderId(), 1);
        return orderId;
    }

    @Override
    public void retrieveOrderRefundEvent(SyncHttpMarketServiceCloseEvent event) {
        // Query the subscription corresponding to the order number
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(event.getSuiteId(),
            event.getCorpId());
        if (StrUtil.isBlank(spaceId)) {
            log.error("Failed to process the refund, DingTalk has not yet bound the space「{}」.",
                event.getCorpId());
            return;
        }
        // Obtain the order ID corresponding to the refunded product.
        // The service order given by the DingTalk event also needs to be processed
        List<String> dingTalkOrderIds =
            iSocialDingTalkOrderService.getOrderIdsByTenantIdAndAppIdAndItemCode(event.getCorpId(),
                event.getSuiteId(), event.getItemCode());
        dingTalkOrderIds.forEach(i -> {
            // Delete the subscription information corresponding to the order
            String orderId = iOrderV2Service.getOrderIdByChannelOrderId(spaceId, i);
            List<String> subscriptionIds =
                iOrderItemService.getSubscriptionIdsByOrderId(orderId).stream()
                    .filter(StrUtil::isNotBlank).collect(Collectors.toList());
            List<String> bundleIds = subscriptionIds.stream().map(subscriptionId -> {
                SubscriptionEntity subscription =
                    iSubscriptionService.getBySubscriptionId(subscriptionId);
                if (null != subscription) {
                    return subscription.getBundleId();
                }
                return null;
            }).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if (!subscriptionIds.isEmpty()) {
                iSubscriptionService.removeBatchBySubscriptionIds(subscriptionIds);
            }
            if (!bundleIds.isEmpty()) {
                iBundleService.removeBatchByBundleIds(bundleIds);
            }
        });
        // Refund processing completed
        iSocialDingTalkRefundService.updateTenantRefundStatusByOrderId(event.getCorpId(),
            event.getSuiteId(),
            event.getOrderId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrateEvent(String spaceId) {
        // Read binding information
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (null == bindInfo) {
            log.warn("DingTalk space is not bound.");
            return;
        }
        List<CorpBizDataDto> bizDataList =
            iDingTalkInternalService.getCorpBizDataByBizTypes(bindInfo.getAppId(),
                bindInfo.getTenantId(),
                ListUtil.toList(DingTalkBizType.MARKET_ORDER, DingTalkBizType.SUBSCRIPTION_CLOSE));
        bizDataList.forEach(i -> {
            if (DingTalkBizType.MARKET_ORDER.getValue().equals(i.getBizType())) {
                // Check if it has been processed
                SyncHttpMarketOrderEvent event =
                    JSONUtil.toBean(i.getBizData(), SyncHttpMarketOrderEvent.class);
                Integer status =
                    iSocialDingTalkOrderService.getStatusByOrderId(bindInfo.getTenantId(),
                        bindInfo.getAppId(),
                        i.getBizId());
                if (null == status) {
                    iSocialDingTalkOrderService.createOrder(event);
                }
                // not processed
                if (!SqlHelper.retBool(status)) {
                    retrieveOrderPaidEvent(event);
                }
            }
            if (DingTalkBizType.SUBSCRIPTION_CLOSE.getValue().equals(i.getBizType())) {
                SyncHttpMarketServiceCloseEvent event = JSONUtil.toBean(i.getBizData(),
                    SyncHttpMarketServiceCloseEvent.class);
                Integer status =
                    iSocialDingTalkRefundService.getStatusByOrderId(bindInfo.getTenantId(),
                        bindInfo.getAppId(), event.getOrderId());
                if (null == status) {
                    iSocialDingTalkRefundService.createRefund(event);
                }
                // not processed
                if (!SqlHelper.retBool(status)) {
                    retrieveOrderRefundEvent(
                        JSONUtil.toBean(i.getBizData(), SyncHttpMarketServiceCloseEvent.class));
                }
            }
        });
    }

    @Override
    public SocialOrderContext buildSocialOrderContext(SyncHttpMarketOrderEvent event) {
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(event.getSuiteId(),
            event.getCorpId());
        if (StrUtil.isBlank(spaceId)) {
            log.warn("DingTalk enterprise「{}」 has not received the application activation event",
                event.getCorpId());
            return null;
        }
        // Paid plan for order purchase
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        // If the price is null, then the basic version of DingTalk is purchased
        Product product = ObjectUtil.isNull(price)
            ? BillingConfigManager.getCurrentFreeProduct(ProductChannel.DINGTALK)
            : BillingConfigManager.getBillingConfig().getProducts().get(price.getProduct());
        SubscriptionPhase phase =
            DingTalkOrderChargeType.TRYOUT.getValue().equals(event.getOrderChargeType())
                ? SubscriptionPhase.TRIAL : SubscriptionPhase.FIXEDTERM;
        SocialOrderContext orderContext = SocialOrderContext.builder()
            .productChannel(ProductChannel.DINGTALK)
            .socialOrderId(event.getOrderId())
            .amount(event.getPayFee())
            .price(price)
            .orderChannel(OrderChannel.DINGTALK)
            .product(product)
            .spaceId(spaceId)
            .orderType(OrderType.of(event.getOrderType()))
            .paidTime(ClockUtil.milliToLocalDateTime(event.getPaidtime(), ZoneOffset.ofHours(8)))
            .createdTime(ClockUtil.milliToLocalDateTime(event.getPaidtime(), ZoneOffset.ofHours(8)))
            .phase(phase)
            .serviceStartTime(
                ClockUtil.milliToLocalDateTime(event.getServiceStartTime(), ZoneOffset.ofHours(8)))
            .serviceStopTime(
                ClockUtil.milliToLocalDateTime(event.getServiceStopTime(), ZoneOffset.ofHours(8)))
            .build();
        if (null != event.getDiscountFee() && event.getDiscountFee() > 0) {
            orderContext.setDiscountAmount(event.getDiscountFee());
            orderContext.setOriginalAmount(event.getPayFee() + event.getDiscountFee());
        }
        // unified for renewal
        if (event.getOrderType().equals(DingTalkOrderType.RENEW_DEGRADE.getValue())
            || event.getOrderType().equals(DingTalkOrderType.RENEW_UPGRADE.getValue())) {
            orderContext.setOrderType(OrderType.RENEW);
        }
        orderContext.setActivatedBundle(iBundleService.getActivatedBundleBySpaceId(spaceId));
        return orderContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SocialOrderStrategyFactory.register(SocialPlatformType.DINGTALK, this);
    }
}
