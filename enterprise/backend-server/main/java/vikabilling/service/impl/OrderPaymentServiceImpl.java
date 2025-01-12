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

import static com.apitable.enterprise.vikabilling.enums.BundleState.ACTIVATED;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.social.notification.SocialNotificationManagement;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;
import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;
import com.apitable.enterprise.vikabilling.entity.OrderPaymentEntity;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.enums.OrderException;
import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.mapper.OrderPaymentMapper;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderPaymentService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.shared.clock.ClockUtil;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.holder.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Order Payment Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class OrderPaymentServiceImpl extends ServiceImpl<OrderPaymentMapper, OrderPaymentEntity>
    implements IOrderPaymentService {

    @Resource
    private IOrderV2Service iOrderV2Service;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Override
    public OrderPaymentEntity getByPayTransactionId(String payTransactionId) {
        return baseMapper.selectByTransactionId(payTransactionId);
    }

    @Override
    public List<OrderPaymentEntity> getByOrderId(String orderId) {
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String retrieveOrderPaidEvent(ChargeSuccess chargeSuccess) {
        String chargeId = chargeSuccess.getId();
        String payTransactionId = chargeSuccess.getOrderNo();
        // Pingxx timezone is utc+8
        LocalDateTime timePaid =
            ClockUtil.secondToLocalDateTime(chargeSuccess.getTimePaid(), ZoneOffset.ofHours(8));
        if (log.isDebugEnabled()) {
            log.debug("Payment time: {}", timePaid);
        }
        OrderPaymentEntity orderPaymentEntity = getByPayTransactionId(payTransactionId);

        if (orderPaymentEntity == null) {
            log.error("Callback. Payment Channel Transaction ID [{}] of order[{}] does not exist.",
                chargeId, payTransactionId);
            throw new BusinessException(OrderException.ORDER_NOT_EXIST);
        }
        if (orderPaymentEntity.getPaymentSuccess()) {
            // The order has been paid and no further processing is required
            log.warn("order [{}] paid success notify repeatedly, ignore this event.", chargeId);
            return null;
        }
        // Query order
        OrderEntity orderEntity = iOrderV2Service.getByOrderId(orderPaymentEntity.getOrderId());
        if (orderEntity == null) {
            log.error("Callback. order[{}] does not exist.", orderPaymentEntity.getOrderId());
            throw new BusinessException(OrderException.ORDER_NOT_EXIST);
        }
        OrderStatus orderStatus = OrderStatus.of(orderEntity.getState());
        if (orderStatus == OrderStatus.FINISHED) {
            // The order has been paid and no further processing is required
            return null;
        }
        // Query the basic product type item in the order item
        OrderItemEntity orderItemEntity =
            iOrderItemService.getBaseProductInOrder(orderPaymentEntity.getOrderId());
        if (orderItemEntity == null) {
            // The order is abnormal, there is no basic product type item
            log.error("Order[{}] exception! Item not included.", orderEntity.getOrderId());
            throw new BusinessException(OrderException.ORDER_EXCEPTION);
        }
        // Paid plan for order purchase
        Plan plan = BillingConfigManager.getPlan(ProductEnum.of(orderItemEntity.getProductName()),
            orderItemEntity.getSeat());
        // Set the operator to update the database
        UserHolder.set(orderPaymentEntity.getCreatedBy());
        // Calculate subscription start and end times based on order type
        OrderType orderType = OrderType.of(orderEntity.getOrderType());
        LocalDateTime entitlementStartDate = ClockManager.me().getLocalDateTimeNow();
        LocalDateTime entitlementExpiredDate;
        String subscriptionId;
        if (orderType == OrderType.BUY) {
            // The expiry time of the rights is calculated directly from the date of payment.
            entitlementExpiredDate = entitlementStartDate.plusMonths(orderItemEntity.getMonths());
            // Create a space station subscription bundle
            BundleEntity bundleEntity = new BundleEntity();
            bundleEntity.setBundleId(UUID.randomUUID().toString());
            bundleEntity.setSpaceId(orderEntity.getSpaceId());
            bundleEntity.setState(ACTIVATED.name());
            bundleEntity.setStartDate(entitlementStartDate);
            bundleEntity.setEndDate(entitlementExpiredDate);

            List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
            // Create base type subscription
            subscriptionId = UUID.randomUUID().toString();
            SubscriptionEntity base = new SubscriptionEntity();
            base.setSpaceId(orderEntity.getSpaceId());
            base.setBundleId(bundleEntity.getBundleId());
            base.setSubscriptionId(subscriptionId);
            base.setProductName(orderItemEntity.getProductName());
            base.setProductCategory(orderItemEntity.getProductCategory());
            base.setPlanId(plan.getId());
            base.setState(SubscriptionState.ACTIVATED.name());
            base.setBundleStartDate(entitlementStartDate);
            base.setStartDate(entitlementStartDate);
            base.setExpireDate(entitlementExpiredDate);
            subscriptionEntities.add(base);

            // New purchase may already have (free subscription + additional subscription)
            Bundle activatedBundle =
                iBundleService.getPossibleBundleBySpaceId(orderEntity.getSpaceId());
            if (activatedBundle != null) {
                activatedBundle.getAddOnSubscription()
                    .stream()
                    .filter(subscription -> {
                        // Filter out unexpired add-on subscriptions
                        LocalDate today = ClockManager.me().getLocalDateNow();
                        LocalDate expireDate = subscription.getExpireDate().toLocalDate();
                        return today.compareTo(expireDate) <= 0;
                    })
                    .forEach(addOnSub -> {
                        // Transfer a non-expired add-on plan subscription to a new subscription
                        SubscriptionEntity addOn = new SubscriptionEntity();
                        addOn.setSpaceId(orderEntity.getSpaceId());
                        addOn.setBundleId(bundleEntity.getBundleId());
                        addOn.setSubscriptionId(addOnSub.getSubscriptionId());
                        addOn.setProductName(addOnSub.getProductName());
                        addOn.setProductCategory(addOnSub.getProductCategory().name());
                        addOn.setPlanId(addOnSub.getPlanId());
                        addOn.setState(SubscriptionState.ACTIVATED.name());
                        addOn.setBundleStartDate(addOnSub.getStartDate());
                        addOn.setStartDate(addOnSub.getStartDate());
                        addOn.setExpireDate(addOnSub.getExpireDate());
                        subscriptionEntities.add(addOn);
                    });
                // expire previous subscriptions
                BundleEntity updateBundle = new BundleEntity();
                updateBundle.setState(BundleState.EXPIRED.name());
                iBundleService.updateByBundleId(activatedBundle.getBundleId(), updateBundle);
            }
            iBundleService.create(bundleEntity);
            iSubscriptionService.createBatch(subscriptionEntities);
        } else {
            // Non-new purchases will have the subscription status start time, and other types of orders will change the space subscription status
            Bundle activatedBundle =
                iBundleService.getActivatedBundleBySpaceId(orderEntity.getSpaceId());
            if (activatedBundle == null) {
                throw new RuntimeException("handle pay success callback error");
            }
            if (orderType == OrderType.RENEW) {
                // Renewal requires extension of benefit end date
                entitlementStartDate = activatedBundle.getBaseSubscription().getExpireDate();
            }
            entitlementExpiredDate = entitlementStartDate.plusMonths(orderItemEntity.getMonths());
            // Change subscription status
            BundleEntity updateBundle = new BundleEntity();
            if (orderType == OrderType.UPGRADE) {
                updateBundle.setStartDate(entitlementStartDate);
            }
            updateBundle.setEndDate(entitlementExpiredDate);
            iBundleService.updateByBundleId(activatedBundle.getBundleId(), updateBundle);

            Subscription baseSubscription = activatedBundle.getBaseSubscription();
            subscriptionId = baseSubscription.getSubscriptionId();
            SubscriptionEntity updateSubscription = new SubscriptionEntity();
            updateSubscription.setProductName(orderItemEntity.getProductName());
            updateSubscription.setProductCategory(orderItemEntity.getProductCategory());
            updateSubscription.setPlanId(plan.getId());
            if (orderType == OrderType.UPGRADE) {
                updateSubscription.setBundleStartDate(entitlementStartDate);
                updateSubscription.setStartDate(entitlementStartDate);
            }
            updateSubscription.setExpireDate(entitlementExpiredDate);
            iSubscriptionService.updateBySubscriptionId(baseSubscription.getSubscriptionId(),
                updateSubscription);
        }

        // Update order status
        OrderEntity updateOrder = new OrderEntity();
        updateOrder.setId(orderEntity.getId());
        updateOrder.setState(OrderStatus.FINISHED.getName());
        updateOrder.setIsPaid(true);
        updateOrder.setPaidTime(timePaid);
        updateOrder.setVersion(orderEntity.getVersion());
        iOrderV2Service.updateById(updateOrder);

        // Update order sub-items
        OrderItemEntity updateOrderItem = new OrderItemEntity();
        updateOrderItem.setId(orderItemEntity.getId());
        updateOrderItem.setSubscriptionId(subscriptionId);
        updateOrderItem.setStartDate(entitlementStartDate);
        updateOrderItem.setEndDate(entitlementExpiredDate);
        iOrderItemService.updateById(updateOrderItem);

        // Update payment order status
        OrderPaymentEntity updateOrderPayment = new OrderPaymentEntity();
        updateOrderPayment.setId(orderPaymentEntity.getId());
        updateOrderPayment.setPaidTime(timePaid);
        updateOrderPayment.setPaymentSuccess(true);
        updateOrderPayment.setRawData(chargeSuccess.toString());
        updateOrderPayment.setVersion(orderPaymentEntity.getVersion());
        updateById(updateOrderPayment);
        if (StrUtil.isBlank(orderPaymentEntity.getPayChannelTransactionId())) {
            updateOrderPayment.setPayChannelTransactionId(chargeSuccess.getTransactionNo());
        }
        // Send notification
        TaskManager.me().execute(() -> SocialNotificationManagement.me()
            .sendSubscribeNotify(orderEntity.getSpaceId(), orderEntity.getCreatedBy(),
                LocalDateTimeUtil.toEpochMilli(entitlementExpiredDate),
                updateOrderPayment.getSubject(),
                orderEntity.getAmount(), orderEntity.getOrderType()));

        return orderEntity.getOrderId();
    }
}
