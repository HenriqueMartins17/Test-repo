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

import static com.apitable.enterprise.vikabilling.util.BillingUtil.legacyPlanId;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.calculateProrationBetweenDates;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.calculateUnusedAmount;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.centsToYuan;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.toCurrencyUnit;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.yuanToCents;
import static java.time.temporal.ChronoUnit.DAYS;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.social.notification.SocialNotificationManagement;
import com.apitable.enterprise.vikabilling.autoconfigure.properties.PingProperties;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.ComparableProduct;
import com.apitable.enterprise.vikabilling.core.DryRunArguments;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderPrice;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;
import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;
import com.apitable.enterprise.vikabilling.entity.OrderPaymentEntity;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.Currency;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderException;
import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.mapper.OrderMapper;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.model.OrderDetailVo;
import com.apitable.enterprise.vikabilling.model.OrderPaymentVo;
import com.apitable.enterprise.vikabilling.model.OrderPreview;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderPaymentService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.ChargeManager;
import com.apitable.enterprise.vikabilling.util.OrderUtil;
import com.apitable.enterprise.vikabilling.util.model.BillingPlanPrice;
import com.apitable.enterprise.vikabilling.util.model.ChargeDTO;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingplusplus.model.Charge;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Order Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class OrderV2ServiceImpl extends ServiceImpl<OrderMapper, OrderEntity>
    implements IOrderV2Service {

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IOrderPaymentService iOrderPaymentService;

    @Autowired(required = false)
    private PingProperties pingProperties;

    @Resource
    private ChargeManager chargeManager;

    @Override
    public OrderEntity getByOrderId(String orderId) {
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    public List<OrderEntity> getByOrderIds(List<String> orderIds) {
        return baseMapper.selectByOrderIds(orderIds);
    }

    @Override
    public OrderDetailVo getOrderDetailByOrderId(String orderId) {
        OrderEntity orderEntity = getByOrderId(orderId);
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrderNo(orderEntity.getOrderId());
        orderDetailVo.setPriceOrigin(centsToYuan(orderEntity.getOriginalAmount()));
        orderDetailVo.setPricePaid(centsToYuan(orderEntity.getAmount()));
        orderDetailVo.setStatus(
            Objects.requireNonNull(OrderStatus.of(orderEntity.getState())).getName());
        orderDetailVo.setCreatedTime(orderEntity.getCreatedAt());
        return orderDetailVo;
    }

    @Override
    public OrderPreview triggerDryRunOrderGeneration(DryRunArguments dryRunArguments) {
        String spaceId = dryRunArguments.getSpaceId();
        OrderPreview orderPreview = new OrderPreview();
        orderPreview.setSpaceId(spaceId);
        orderPreview.setCurrency(Currency.CNY.name());
        // Find change paid plans
        Price newPricePlan =
            BillingConfigManager.getPriceBySeatAndMonth(dryRunArguments.getProduct(),
                dryRunArguments.getSeat(), dryRunArguments.getMonth());
        if (newPricePlan == null) {
            throw new BusinessException(OrderException.PLAN_NOT_EXIST);
        }
        // Get subscription information for a space
        Bundle activeBundle = iBundleService.getActivatedBundleBySpaceId(spaceId);
        // Set order type
        orderPreview.setOrderType(parseOrderType(activeBundle, newPricePlan));
        switch (dryRunArguments.getAction()) {
            case START_BILLING:
            case RENEW:
                BillingPlanPrice planPrice =
                    BillingPlanPrice.of(newPricePlan, ClockManager.me().getLocalDateNow());
                orderPreview.setPriceOrigin(planPrice.getOrigin());
                orderPreview.setPriceDiscount(planPrice.getDiscount());
                orderPreview.setPricePaid(planPrice.getActual());
                break;
            case UPGRADE:
                OrderPrice orderPrice = repairOrderPrice(activeBundle, newPricePlan);
                orderPreview.setPriceOrigin(toCurrencyUnit(orderPrice.getPriceOrigin()));
                orderPreview.setPriceUnusedCalculated(
                    toCurrencyUnit(orderPrice.getPriceUnusedCalculated()));
                orderPreview.setPriceDiscount(toCurrencyUnit(orderPrice.getPriceDiscount()));
                if (orderPrice.getPricePaid().compareTo(BigDecimal.ZERO) <= 0) {
                    // A negative number means that the deducted amount is greater than the amount to be paid, which is actually 0 yuan
                    orderPreview.setPricePaid(BigDecimal.ZERO);
                } else {
                    orderPreview.setPricePaid(toCurrencyUnit(orderPrice.getPricePaid()));
                }
                break;
            default:
                throw new IllegalArgumentException(
                    "Unexpected dryRunArguments action " + dryRunArguments.getAction());
        }
        return orderPreview;
    }

    @Override
    public OrderPrice repairOrderPrice(Bundle activeBundle, Price newPricePlan) {
        if (activeBundle == null) {
            throw new RuntimeException("Space has not subscription ");
        }
        // Subscriptions about to change
        Subscription subscriptionForChange = activeBundle.getBaseSubscription();
        List<OrderItemEntity> orderItemEntities =
            iOrderItemService.getBySubscriptionId(subscriptionForChange.getSubscriptionId());
        if (orderItemEntities.isEmpty()) {
            throw new RuntimeException("can not repair price for upgrade order");
        }
        List<String> orderIds = orderItemEntities.stream().map(OrderItemEntity::getOrderId)
            .collect(Collectors.toList());
        List<OrderEntity> orderEntities = getByOrderIds(orderIds);
        int[] findUpgradeOrderItemIndexes = CollectionUtil.findIndex(orderEntities,
            order -> OrderType.of(order.getOrderType()) == OrderType.UPGRADE);
        // The total amount of the original paid plan
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (findUpgradeOrderItemIndexes.length > 0) {
            // Calculate the order amount by stacking from the last cursor
            int last = findUpgradeOrderItemIndexes[findUpgradeOrderItemIndexes.length - 1];
            String lastUpgradeOrderId = orderEntities.get(last).getOrderId();
            int[] indexOfAll = CollectionUtil.findIndex(orderItemEntities,
                orderItem -> orderItem.getOrderId().equals(lastUpgradeOrderId));
            int lastItem = indexOfAll[0];
            for (int i = lastItem; i < orderItemEntities.size(); i++) {
                OrderItemEntity orderItem = orderItemEntities.get(i);
                totalAmount = totalAmount.add(centsToYuan(orderItem.getAmount()));
            }
        } else {
            totalAmount = orderItemEntities.stream()
                .map(item -> centsToYuan(item.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (log.isDebugEnabled()) {
            log.debug("old paid plan amount: {}", totalAmount);
        }
        // old plan term
        LocalDate startDate = activeBundle.getBundleStartDate().toLocalDate();
        LocalDate endDate = activeBundle.getBaseSubscription().getExpireDate().toLocalDate();
        // Total days of old plan
        long nbTotalDays = DAYS.between(startDate, endDate);
        // targetDate calculates the proportion of days in use, this method is only used in China, using the Chinese time zone
        LocalDate targetDate = ClockManager.me().getLocalDateNow();
        BigDecimal usedDaysProrated =
            calculateProrationBetweenDates(startDate, targetDate, nbTotalDays);
        if (log.isDebugEnabled()) {
            log.debug("used pro-rated: {}", usedDaysProrated);
        }
        // Remaining amount of old plan
        BigDecimal unusedCalculatedAmount = calculateUnusedAmount(totalAmount, usedDaysProrated);
        if (log.isDebugEnabled()) {
            log.debug("unused plan calculated amount: {}", unusedCalculatedAmount);
        }
        // Change the price of the paid plan (unit: yuan)
        BillingPlanPrice planPrice =
            BillingPlanPrice.of(newPricePlan, ClockManager.me().getLocalDateNow());
        BigDecimal upgradePlanAmount = planPrice.getActual();
        if (log.isDebugEnabled()) {
            log.debug("ready change plan amount: {}", upgradePlanAmount);
        }
        // Payment amount
        BigDecimal paidAmount = upgradePlanAmount.subtract(unusedCalculatedAmount);
        return new OrderPrice(upgradePlanAmount, unusedCalculatedAmount, unusedCalculatedAmount,
            paidAmount);
    }

    @Override
    public OrderType parseOrderType(Bundle activeBundle, Price newPricePlan) {
        // According to the request parameters, analyze the order type: new purchase, renewal, upgrade
        OrderType orderType = OrderType.BUY;
        if (activeBundle == null) {
            // No subscription, return new purchase type
            return orderType;
        }
        // There is an active subscription, determine the subscription level of the basic type product, and confirm whether to renew or upgrade
        Subscription baseProductSubscription = activeBundle.getBaseSubscription();
        // Subscription plans for the current space station
        Plan currentPricePlan = BillingConfigManager.getBillingConfig().getPlans()
            .get(legacyPlanId(baseProductSubscription.getPlanId()));
        ComparableProduct currentProduct =
            new ComparableProduct(ProductEnum.of(currentPricePlan.getProduct()));
        if (currentProduct.getProduct().isFree()) {
            return orderType;
        }
        // Requested subscription plan
        ComparableProduct requestProduct =
            new ComparableProduct(ProductEnum.of(newPricePlan.getProduct()));
        if (requestProduct.isEqual(currentProduct)) {
            // Request the same product type and judge the difference in the number of people in the plan
            if (newPricePlan.getSeat() > currentPricePlan.getSeats()) {
                // The number of people is larger than the current plan, upgrade the order
                orderType = OrderType.UPGRADE;
            } else if (newPricePlan.getSeat().equals(currentPricePlan.getSeats())) {
                // The same number of people, renew the order
                orderType = OrderType.RENEW;
            } else {
                log.error("Pull subscription downgrades are not allowed");
                throw new BusinessException(OrderException.NOT_ALLOW_DOWNGRADE);
            }
        } else if (requestProduct.isGreaterThan(currentProduct)) {
            // Upgrade request
            orderType = OrderType.UPGRADE;
        } else if (requestProduct.isLessThan(currentProduct)) {
            // Downgrade is not allowed, order creation is refused
            log.error("Pull subscription downgrades are not allowed");
            throw new BusinessException(OrderException.NOT_ALLOW_DOWNGRADE);
        }
        return orderType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderArguments orderArguments) {
        Price price = orderArguments.getPrice();
        // Judging whether the scheme exists or not, it is abnormal if it does not exist
        if (price == null) {
            throw new BusinessException(OrderException.PLAN_NOT_EXIST);
        }
        // Get subscription information for a space
        Bundle activeBundle =
            iBundleService.getActivatedBundleBySpaceId(orderArguments.getSpaceId());
        OrderType orderType = parseOrderType(activeBundle, price);
        if (orderType == OrderType.BUY) {
            // In the new purchase scenario, there must be no basic subscription in the current space
            if (activeBundle != null && !activeBundle.isBaseForFree()) {
                throw new BusinessException(OrderException.REPEAT_NEW_BUY_ORDER);
            }
        }
        BillingPlanPrice planPrice =
            BillingPlanPrice.of(price, ClockManager.me().getLocalDateNow());
        OrderPrice orderPrice =
            new OrderPrice(price.getOriginPrice(), planPrice.getDiscount(), planPrice.getDiscount(),
                planPrice.getActual());
        // The self-operated 0 yuan order currently only occurs when the subscription is upgraded
        boolean isZeroOrder = false;
        if (orderType == OrderType.UPGRADE) {
            orderPrice = repairOrderPrice(activeBundle, price);
            if (orderPrice.getPricePaid().compareTo(BigDecimal.ZERO) <= 0) {
                orderPrice.setPricePaid(BigDecimal.ZERO);
                isZeroOrder = true;
            }
        }
        int originalAmount = yuanToCents(orderPrice.getPriceOrigin());
        int discountAmount = yuanToCents(orderPrice.getPriceDiscount());
        int amount = yuanToCents(orderPrice.getPricePaid());
        // Create order
        String orderId = OrderUtil.createOrderId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setSpaceId(orderArguments.getSpaceId());
        orderEntity.setOrderId(orderId);
        orderEntity.setOrderChannel(OrderChannel.VIKA.getName());
        orderEntity.setOrderType(orderType.name());
        orderEntity.setCurrency(Currency.CNY.name());
        orderEntity.setOriginalAmount(originalAmount);
        orderEntity.setDiscountAmount(discountAmount);
        orderEntity.setAmount(amount);
        LocalDateTime nowDateTime = ClockManager.me().getLocalDateTimeNow();
        if (isZeroOrder) {
            // 0 yuan to create a space subscription directly
            // Here we should uniformly use the benefit service class to manage space subscriptions
            orderEntity.setState(OrderStatus.FINISHED.getName());
            orderEntity.setIsPaid(true);
            orderEntity.setPaidTime(nowDateTime);
        } else {
            orderEntity.setState(OrderStatus.UNPAID.getName());
        }
        orderEntity.setCreatedTime(nowDateTime);
        save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setSpaceId(orderArguments.getSpaceId());
        orderItemEntity.setOrderId(orderId);
        orderItemEntity.setAmount(amount);
        orderItemEntity.setProductName(price.getProduct());
        Product product =
            BillingConfigManager.getBillingConfig().getProducts().get(price.getProduct());
        orderItemEntity.setProductCategory(product.getCategory());
        orderItemEntity.setPlanId(price.getPlanId());
        orderItemEntity.setSeat(price.getSeat());
        orderItemEntity.setMonths(price.getMonth());
        orderItemEntity.setCurrency(Currency.CNY.name());
        orderItemEntity.setAmount(amount);
        if (isZeroOrder) {
            String subscriptionId = getSubscriptionId(activeBundle);
            orderItemEntity.setSubscriptionId(subscriptionId);
            orderItemEntity.setStartDate(nowDateTime);
            orderItemEntity.setEndDate(nowDateTime.plusMonths(orderItemEntity.getMonths()));
        }
        iOrderItemService.save(orderItemEntity);

        if (isZeroOrder) {
            // Expiration
            LocalDateTime entitlementExpiredDate =
                nowDateTime.plusMonths(orderItemEntity.getMonths());
            // Change subscription status
            BundleEntity updateBundle = new BundleEntity();
            updateBundle.setEndDate(entitlementExpiredDate);
            iBundleService.updateByBundleId(activeBundle.getBundleId(), updateBundle);

            SubscriptionEntity updateSubscription = new SubscriptionEntity();
            updateSubscription.setProductName(orderItemEntity.getProductName());
            updateSubscription.setProductCategory(orderItemEntity.getProductCategory());
            updateSubscription.setPlanId(price.getPlanId());
            updateSubscription.setStartDate(nowDateTime);
            updateSubscription.setExpireDate(entitlementExpiredDate);
            iSubscriptionService.updateBySubscriptionId(getSubscriptionId(activeBundle),
                updateSubscription);

            TaskManager.me().execute(() -> SocialNotificationManagement.me()
                .sendSubscribeNotify(orderEntity.getSpaceId(), orderEntity.getCreatedBy(),
                    LocalDateTimeUtil.toEpochMilli(entitlementExpiredDate), price.getGoodChTitle(),
                    orderEntity.getAmount(), orderType.name()));
        }
        return orderId;
    }

    private String getSubscriptionId(Bundle bundle) {
        Subscription baseSubscription = bundle.getBaseSubscription();
        return baseSubscription.getSubscriptionId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentVo createOrderPayment(Long userId, String orderId, PayChannel channel) {
        // Check if order exists
        OrderEntity orderEntity = getByOrderId(orderId);
        if (orderEntity == null) {
            throw new BusinessException(OrderException.ORDER_NOT_EXIST);
        }
        if (orderEntity.getAmount() == 0) {
            // 0 yuan is not allowed to create payment orders
            throw new BusinessException(OrderException.ORDER_EXCEPTION);
        }
        // Check if user is in this space
        iMemberService.checkUserIfInSpace(userId, orderEntity.getSpaceId());
        // Check if the order is unpaid
        OrderStatus orderStatus = OrderStatus.of(orderEntity.getState());
        if (orderStatus != OrderStatus.UNPAID) {
            // Other states do not allow payment
            if (orderStatus == OrderStatus.CANCELED) {
                throw new BusinessException(OrderException.ORDER_HAS_CANCELED);
            }
            if (orderStatus == OrderStatus.FINISHED) {
                throw new BusinessException(OrderException.ORDER_HAS_PAID);
            }
        }
        List<OrderItemEntity> orderItemEntities = iOrderItemService.getByOrderId(orderId);
        if (orderItemEntities.isEmpty()) {
            throw new BusinessException(OrderException.ORDER_EXCEPTION);
        }
        // Only provide basic product purchase, so there is only one
        OrderItemEntity orderItem = orderItemEntities.iterator().next();
        Price price = BillingConfigManager.getPriceBySeatAndMonth(
            orderItem.getProductName().toUpperCase(Locale.ROOT), orderItem.getSeat(),
            orderItem.getMonths());
        if (price == null) {
            throw new BusinessException(OrderException.ORDER_EXCEPTION);
        }

        String payTransactionId = OrderUtil.createPayTransactionId();
        int actualAmount = orderEntity.getAmount();
        // Total order amount, RMB unit: cents (if the total order amount is 1 RMB, please fill in 100 here)
        if (pingProperties.isTestMode()) {
            // In test mode, it is 1 yuan
            actualAmount = 100;
        }

        // Create transaction request
        ChargeDTO charge =
            chargeManager.createCharge(price, channel, payTransactionId, actualAmount);

        // Create payment order
        OrderPaymentEntity orderPaymentEntity = new OrderPaymentEntity();
        orderPaymentEntity.setOrderId(orderId);
        orderPaymentEntity.setPaymentTransactionId(payTransactionId);
        orderPaymentEntity.setCurrency(orderEntity.getCurrency());
        orderPaymentEntity.setAmount(orderEntity.getAmount());
        orderPaymentEntity.setSubject(price.getGoodChTitle());
        orderPaymentEntity.setPayChannel(channel.getName());
        orderPaymentEntity.setPayChannelTransactionId(charge.getChannelTransactionId());
        iOrderPaymentService.save(orderPaymentEntity);
        // Return to payment order view
        OrderPaymentVo paymentVo = new OrderPaymentVo();
        paymentVo.setOrderNo(orderId);
        paymentVo.setPayTransactionNo(payTransactionId);
        if (PayChannel.isWechatpay(channel)) {
            paymentVo.setWxQrCodeLink(charge.getWxQrCodeLink());
        } else if (PayChannel.isAlipay(channel)) {
            paymentVo.setAlipayPcDirectCharge(charge.getAlipayPcDirectCharge());
        }
        return paymentVo;
    }

    @Override
    public OrderStatus getOrderStatusByOrderId(String orderId) {
        // Check if order exists
        OrderEntity orderEntity = getByOrderId(orderId);
        if (orderEntity == null) {
            throw new BusinessException(OrderException.ORDER_NOT_EXIST);
        }
        OrderStatus orderStatus = OrderStatus.of(orderEntity.getState());
        if (orderStatus == null) {
            return OrderStatus.UNPAID;
        }
        return orderStatus;
    }

    @Override
    public String getOrderIdByChannelOrderId(String spaceId, String channelOrderId) {
        return baseMapper.selectOrderBySpaceIdChannelOrderId(spaceId, channelOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatus checkOrderStatus(String orderId) {
        List<OrderPaymentEntity> orderPaymentEntities = iOrderPaymentService.getByOrderId(orderId);
        if (orderPaymentEntities.isEmpty()) {
            return OrderStatus.CANCELED;
        }
        OrderPaymentEntity orderPaymentEntity = orderPaymentEntities.iterator().next();
        if (orderPaymentEntity.getPaymentSuccess()) {
            return OrderStatus.FINISHED;
        }
        String payChannelTransactionId = orderPaymentEntity.getPayChannelTransactionId();
        if (StrUtil.isBlank(payChannelTransactionId)) {
            return OrderStatus.CANCELED;
        }
        Charge charge = retrieveByChargeId(payChannelTransactionId);
        if (charge == null) {
            throw new BusinessException(OrderException.PAYMENT_ORDER_NOT_EXIST);
        }
        if (!charge.getPaid()) {
            return OrderStatus.UNPAID;
        }
        // The payment has been successful, and there is a notification of payment success delay, which will be processed immediately
        ChargeSuccess chargeSuccess = ChargeSuccess.build(charge);
        iOrderPaymentService.retrieveOrderPaidEvent(chargeSuccess);
        // Sync order events
        SpringContextHolder.getApplicationContext().publishEvent(new SyncOrderEvent(this, orderId));
        return OrderStatus.FINISHED;
    }

    private Charge retrieveByChargeId(String chargeId) {
        try {
            return Charge.retrieve(chargeId);
        } catch (Exception e) {
            log.error("Failed to query Ping++ payment order.", e);
            throw new BusinessException(OrderException.ORDER_EXCEPTION);
        }
    }
}
