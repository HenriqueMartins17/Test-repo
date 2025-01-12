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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Resource;

import com.vikadata.social.feishu.card.Card;
import com.vikadata.social.feishu.card.CardComponent;
import com.vikadata.social.feishu.card.CardMessage;
import com.vikadata.social.feishu.card.Config;
import com.vikadata.social.feishu.card.Header;
import com.vikadata.social.feishu.card.Message;
import com.vikadata.social.feishu.card.TemplateColor;
import com.vikadata.social.feishu.card.module.Div;
import com.vikadata.social.feishu.card.module.Hr;
import com.vikadata.social.feishu.card.module.Module;
import com.vikadata.social.feishu.card.module.Note;
import com.vikadata.social.feishu.card.objects.Text;
import com.vikadata.social.feishu.card.objects.Text.Mode;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;
import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.enums.Currency;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.model.OfflineOrderInfo;
import com.apitable.enterprise.vikabilling.model.SpaceSubscriptionVo;
import com.apitable.enterprise.vikabilling.service.IBillingOfflineService;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.OrderUtil;
import com.apitable.enterprise.gm.ro.CreateBusinessOrderRo;
import com.apitable.enterprise.gm.ro.CreateEntitlementWithAddOn;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.service.ISpaceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.enterprise.vikabilling.enums.BundleState.ACTIVATED;
import static com.apitable.enterprise.vikabilling.util.BillingUtil.legacyPlanId;
import static com.apitable.shared.util.AssertUtil.verifyNonNullOrEmpty;

/**
 * <p>
 * Financial Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class BillingOfflineServiceImpl implements IBillingOfflineService {

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IOrderV2Service iOrderV2Service;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Resource
    private ISpaceSubscriptionService iSpaceSubscriptionService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Override
    public SpaceSubscriptionVo getSpaceSubscription(String spaceId) {
        SubscriptionInfo planInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        SpaceSubscriptionVo vo = new SpaceSubscriptionVo();
        vo.setSpaceId(spaceId);
        vo.setProduct(planInfo.getProduct());
        vo.setSeats(BillingConfigManager.getBillingConfig().getPlans().get(planInfo.getBasePlan())
            .getSeats());
        vo.setStartDate(planInfo.getStartDate());
        vo.setEndDate(planInfo.getEndDate());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfflineOrderInfo createBusinessOrder(Long userId, CreateBusinessOrderRo data) {
        String spaceId = data.getSpaceId();
        // Check if space exists
        SpaceEntity spaceEntity = iSpaceService.getBySpaceId(spaceId);
        // Validate the order of the space according to the order type
        OrderType orderType = OrderType.of(data.getType());
        if (orderType == null) {
            throw new IllegalArgumentException("Incorrect order type.");
        }
        if (orderType == OrderType.BUY || orderType == OrderType.UPGRADE) {
            verifyNonNullOrEmpty(data.getProduct(), "product should be specified");
            verifyNonNullOrEmpty(data.getSeat(), "seat should be specified");
        }
        // Query the subscription status of a space
        Bundle activeBundle = iBundleService.getActivatedBundleBySpaceId(spaceId);
        // Check whether the space exists for self-operated
        if (orderType == OrderType.BUY) {
            // Duplicate new purchase order, operation not allowed
            if (activeBundle != null && !activeBundle.isBaseForFree()) {
                throw new IllegalArgumentException(
                    "Can't place an order, the space station already has a subscription, maybe you need to renew and upgrade.");
            }
        } else if (orderType == OrderType.RENEW) {
            if (activeBundle == null) {
                throw new IllegalArgumentException(
                    "Unable to renew, space station has no subscription.");
            }
        } else if (orderType == OrderType.UPGRADE) {
            if (activeBundle == null) {
                throw new IllegalArgumentException(
                    "Can't upgrade, space station doesn't have a subscription.");
            }
        }
        String inputDate = data.getStartDate();
        LocalDateTime startDate = inputDate == null || inputDate.isEmpty() ?
            ClockManager.me().getLocalDateTimeNow() :
            LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();

        OfflineOrderInfo message;
        switch (orderType) {
            case BUY:
                message =
                    createNewBuyOrder(spaceEntity, data.getProduct(), data.getSeat(), startDate,
                        data.getMonths(), data.getRemark(), userId);
                break;
            case RENEW:
                message =
                    createRenewOrder(spaceEntity, activeBundle, data.getMonths(), data.getRemark(),
                        userId);
                break;
            case UPGRADE:
                message =
                    createUpgradeOrder(spaceEntity, activeBundle, data.getProduct(), data.getSeat(),
                        startDate, data.getMonths(), data.getRemark(), userId);
                break;
            default:
                throw new RuntimeException("unknown order type");
        }
        return message;
    }

    private OfflineOrderInfo createNewBuyOrder(SpaceEntity space, String product, int seat,
                                               LocalDateTime startDate, int months, String remark,
                                               Long createdBy) {
        Plan plan = BillingConfigManager.getPlan(product, seat);
        LocalDateTime endDate = startDate.plusMonths(months);
        String subscriptionId =
            createSubscription(space.getSpaceId(), plan, startDate, endDate, createdBy);
        String orderId =
            createOrder(space.getSpaceId(), OrderType.BUY, plan, months, subscriptionId, startDate,
                endDate, remark, createdBy);
        Message message =
            buildNewBuyCard(space.getSpaceId(), space.getName(), plan.getProduct(), plan.getSeats(),
                months, startDate.toLocalDate()
                , endDate.toLocalDate(), remark);
        return OfflineOrderInfo.builder().orderId(orderId).message(message).build();
    }

    private OfflineOrderInfo createRenewOrder(SpaceEntity space, Bundle bundle, int months,
                                              String remark, Long createdBy) {
        LocalDateTime startDate = bundle.getBaseSubscription().getExpireDate();
        LocalDateTime endDate = startDate.plusMonths(months);
        // Update subscription bundle
        BundleEntity updateBundle = BundleEntity.builder().endDate(endDate).updatedBy(-1L).build();
        iBundleService.updateByBundleId(bundle.getBundleId(), updateBundle);
        // Update subscription
        Subscription subscription = bundle.getBaseSubscription();
        String subscriptionId = subscription.getSubscriptionId();
        SubscriptionEntity updateSubscription =
            SubscriptionEntity.builder().expireDate(endDate).updatedBy(-1L).build();
        iSubscriptionService.updateBySubscriptionId(subscriptionId, updateSubscription);
        Plan plan = BillingConfigManager.getBillingConfig().getPlans()
            .get(legacyPlanId(subscription.getPlanId()));
        String orderId =
            createOrder(space.getSpaceId(), OrderType.RENEW, plan, months, subscriptionId,
                startDate, endDate, remark, createdBy);
        Message message =
            buildRenewCard(space.getSpaceId(), space.getName(), plan.getProduct(), plan.getSeats(),
                months, startDate.toLocalDate()
                , endDate.toLocalDate(), remark);
        return OfflineOrderInfo.builder().orderId(orderId).message(message).build();
    }

    private OfflineOrderInfo createUpgradeOrder(SpaceEntity space, Bundle bundle, String product,
                                                int seat, LocalDateTime startDate, int months,
                                                String remark, Long createdBy) {
        LocalDateTime endDate = startDate.plusMonths(months);
        // Update subscription bundle
        BundleEntity updateBundle =
            BundleEntity.builder().startDate(startDate).endDate(endDate).updatedBy(-1L).build();
        iBundleService.updateByBundleId(bundle.getBundleId(), updateBundle);
        // Update subscription
        Subscription subscription = bundle.getBaseSubscription();
        String subscriptionId = subscription.getSubscriptionId();
        Plan plan = BillingConfigManager.getPlan(product, seat);
        SubscriptionEntity updateSubscription = SubscriptionEntity.builder()
            .productName(plan.getProduct())
            .productCategory(plan.getProductCategory()).planId(plan.getId())
            .bundleStartDate(startDate).startDate(startDate).expireDate(endDate).updatedBy(-1L)
            .build();
        iSubscriptionService.updateBySubscriptionId(subscriptionId, updateSubscription);
        String orderId =
            createOrder(space.getSpaceId(), OrderType.UPGRADE, plan, months, subscriptionId,
                startDate, endDate, remark, createdBy);
        Message message =
            buildUpgradeCard(space.getSpaceId(), space.getName(), subscription.getProductName(),
                plan.getProduct(), plan.getSeats(), months, startDate.toLocalDate()
                , endDate.toLocalDate(), remark);
        return OfflineOrderInfo.builder().orderId(orderId).message(message).build();
    }

    public String createSubscription(String spaceId, Plan plan, LocalDateTime startDate,
                                     LocalDateTime endDate, Long createdBy) {
        // Create a space station subscription bundle
        BundleEntity bundleEntity = createBundle(spaceId, startDate, endDate, createdBy);

        List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        // Create base type subscription
        String subscriptionId = UUID.randomUUID().toString();
        SubscriptionEntity baseSubscription = new SubscriptionEntity();
        baseSubscription.setSpaceId(spaceId);
        baseSubscription.setBundleId(bundleEntity.getBundleId());
        baseSubscription.setSubscriptionId(subscriptionId);
        baseSubscription.setProductName(plan.getProduct());
        Product product =
            BillingConfigManager.getBillingConfig().getProducts().get(plan.getProduct());
        baseSubscription.setProductCategory(product.getCategory());
        baseSubscription.setPlanId(plan.getId());
        baseSubscription.setState(SubscriptionState.ACTIVATED.name());
        baseSubscription.setBundleStartDate(startDate);
        baseSubscription.setStartDate(startDate);
        baseSubscription.setExpireDate(endDate);
        baseSubscription.setCreatedBy(-1L);
        baseSubscription.setUpdatedBy(-1L);
        subscriptionEntities.add(baseSubscription);

        // New purchase may already have (free subscription + additional subscription)
        Bundle activatedBundle = iBundleService.getPossibleBundleBySpaceId(spaceId);
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
                    addOn.setSpaceId(spaceId);
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

        return subscriptionId;
    }

    private String createOrder(String spaceId, OrderType orderType, Plan plan, int months,
                               String subscriptionId, LocalDateTime startDate,
                               LocalDateTime endDate, String remark, Long createdBy) {
        // Create order
        String orderId = OrderUtil.createOrderId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setSpaceId(spaceId);
        orderEntity.setOrderId(orderId);
        orderEntity.setOrderChannel(OrderChannel.OFFLINE.getName());
        orderEntity.setOrderType(orderType.name());
        orderEntity.setCurrency(Currency.CNY.name());
        orderEntity.setOriginalAmount(0);
        orderEntity.setDiscountAmount(0);
        orderEntity.setAmount(0);
        orderEntity.setState(OrderStatus.FINISHED.getName());
        LocalDateTime time = ClockManager.me().getLocalDateTimeNow();
        orderEntity.setCreatedTime(time);
        orderEntity.setIsPaid(true);
        orderEntity.setPaidTime(time);
        orderEntity.setCreatedBy(createdBy);
        orderEntity.setUpdatedBy(-1L);
        orderEntity.setRemark(remark);
        iOrderV2Service.save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setSpaceId(spaceId);
        orderItemEntity.setOrderId(orderId);
        orderItemEntity.setAmount(0);
        orderItemEntity.setProductName(plan.getProduct());
        Product product =
            BillingConfigManager.getBillingConfig().getProducts().get(plan.getProduct());
        orderItemEntity.setProductCategory(product.getCategory());
        // There is no fixed payment plan for offline orders, and a more flexible plan combination
        orderItemEntity.setPlanId(plan.getId());
        orderItemEntity.setSeat(plan.getSeats());
        orderItemEntity.setMonths(months);
        orderItemEntity.setCurrency(Currency.CNY.name());
        orderItemEntity.setAmount(0);
        orderItemEntity.setSubscriptionId(subscriptionId);
        orderItemEntity.setStartDate(startDate);
        orderItemEntity.setEndDate(endDate);
        orderItemEntity.setCreatedBy(createdBy);
        orderItemEntity.setUpdatedBy(createdBy);
        iOrderItemService.save(orderItemEntity);

        return orderEntity.getOrderId();
    }

    private Message buildNewBuyCard(String spaceId, String spaceName, String productName, int seat,
                                    int months, LocalDate startDate, LocalDate endDate,
                                    String remark) {
        Header header = new Header(new Text(Mode.PLAIN_TEXT, "New Order Notification", null),
            TemplateColor.GREEN);
        Card card = new Card(new Config(true), header);
        List<Module> divList = new LinkedList<>();
        divList.add(new Div(new Text(Text.Mode.LARK_MD, "**【Order Type】** New Purchase", null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space ID】** " + spaceId, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space Name】** " + spaceName, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Product Grade】** " + productName, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, String.format("**【Seat】** %d seat", seat), null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, String.format("**【Months of Purchase】** %d month", months),
                null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Effective Date】** %s", startDate.toString()), null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Date of Expiry】** %s", endDate.toString()), null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, String.format("**【Remark】** %s", remark), null)));
        divList.add(new Hr());
        List<CardComponent> elements = new ArrayList<>();
        elements.add(new Text(Text.Mode.LARK_MD,
            "[Look over Order](https://vika.cn/workbench/dstK19iRDHJXbcdGH2/viwGztsufm2BB)", null));
        divList.add(new Note(elements));
        // Set content element
        card.setModules(divList);
        return new CardMessage(card.toObj());
    }

    private Message buildRenewCard(String spaceId, String spaceName, String productName, int seat,
                                   int months, LocalDate startDate, LocalDate endDate,
                                   String remark) {
        Header header = new Header(new Text(Mode.PLAIN_TEXT, "Renew Order Notification", null),
            TemplateColor.GREEN);
        Card card = new Card(new Config(true), header);
        List<Module> divList = new LinkedList<>();
        divList.add(new Div(new Text(Text.Mode.LARK_MD, "**【Order Type】** Renew", null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space ID】** " + spaceId, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space Name】** " + spaceName, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Renew Product Grade】** " + productName, null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, String.format("**【Renew Seat】** %d seat", seat), null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Renew Purchase Months】** %d month", months), null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Original Effective Date】** %s", startDate.toString()), null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Expiration Date after Renew】** %s", endDate.toString()), null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, String.format("**【Remark】** %s", remark), null)));
        divList.add(new Hr());
        List<CardComponent> elements = new ArrayList<>();
        elements.add(new Text(Text.Mode.LARK_MD,
            "[Look over Order](https://vika.cn/workbench/dstK19iRDHJXbcdGH2/viwGztsufm2BB)", null));
        divList.add(new Note(elements));
        // Set content element
        card.setModules(divList);
        return new CardMessage(card.toObj());
    }

    private Message buildUpgradeCard(String spaceId, String spaceName, String oldProductName,
                                     String newProductName, int seat, int months,
                                     LocalDate startDate, LocalDate endDate, String remark) {
        Header header = new Header(new Text(Mode.PLAIN_TEXT, "Upgrade Order Notification", null),
            TemplateColor.GREEN);
        Card card = new Card(new Config(true), header);
        List<Module> divList = new LinkedList<>();
        divList.add(new Div(new Text(Text.Mode.LARK_MD, "**【Order Type】** Upgrade", null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space ID】** " + spaceId, null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, "**【Customer Space Name】** " + spaceName, null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, "**【Original Product Grade】** " + oldProductName, null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, "**【Upgrade Product Grade】** " + newProductName, null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, String.format("**【Upgrade Seat】** %d seat", seat), null)));
        divList.add(new Div(
            new Text(Text.Mode.LARK_MD, String.format("**【Months of Purchase】** %d month", months),
                null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Effective Date】** %s", startDate.toString()), null)));
        divList.add(new Div(new Text(Text.Mode.LARK_MD,
            String.format("**【Date of Expiry】** %s", endDate.toString()), null)));
        divList.add(
            new Div(new Text(Text.Mode.LARK_MD, String.format("**【Remark】** %s", remark), null)));
        divList.add(new Hr());
        List<CardComponent> elements = new ArrayList<>();
        elements.add(new Text(Text.Mode.LARK_MD,
            "[Look over Order](https://vika.cn/workbench/dstK19iRDHJXbcdGH2/viwGztsufm2BB)", null));
        divList.add(new Note(elements));
        // Set content element
        card.setModules(divList);
        return new CardMessage(card.toObj());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSubscriptionWithAddOn(CreateEntitlementWithAddOn data, Long createdBy) {
        // Check params
        Plan plan = BillingConfigManager.getBillingConfig().getPlans().get(data.getPlanId());
        verifyNonNullOrEmpty(plan, "Additional plan does not exist");
        String spaceId = data.getSpaceId();
        // Check if space exists
        iSpaceService.checkExist(spaceId);
        if (isSocialBind(spaceId)) {
            throw new IllegalArgumentException(
                "It is not allowed to give away third-party bundled integrated space stations");
        }
        String inputDate = data.getStartDate();
        LocalDateTime startDate = inputDate == null || inputDate.isEmpty() ?
            ClockManager.me().getLocalDateTimeNow() :
            LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        LocalDateTime endDate = startDate.plusMonths(data.getMonths());
        log.info("request start date: {}, end date: {}", startDate, endDate);
        // Query the subscription status of a space
        Bundle activeBundle = iBundleService.getActivatedBundleBySpaceId(spaceId);
        if (activeBundle == null) {
            // Subscription does not exist, create add-on product type
            BundleEntity bundleEntity = createBundle(spaceId, startDate, endDate, createdBy);
            iBundleService.create(bundleEntity);

            List<SubscriptionEntity> entities = new ArrayList<>();
            // Create base type subscription
            String subscriptionId = UUID.randomUUID().toString();
            SubscriptionEntity baseSubscription = new SubscriptionEntity();
            baseSubscription.setSpaceId(spaceId);
            baseSubscription.setBundleId(bundleEntity.getBundleId());
            baseSubscription.setSubscriptionId(subscriptionId);
            Plan freePlan = BillingConfigManager.getFreePlan(ProductChannel.VIKA);
            baseSubscription.setProductName(freePlan.getProduct());
            baseSubscription.setProductCategory(freePlan.getProductCategory());
            baseSubscription.setPlanId(freePlan.getId());
            baseSubscription.setState(SubscriptionState.ACTIVATED.name());
            baseSubscription.setBundleStartDate(startDate);
            baseSubscription.setStartDate(startDate);
            baseSubscription.setExpireDate(endDate);
            baseSubscription.setCreatedBy(createdBy);
            baseSubscription.setUpdatedBy(createdBy);
            entities.add(baseSubscription);

            // Add-on type subscription
            SubscriptionEntity addSubscription = new SubscriptionEntity();
            addSubscription.setSpaceId(spaceId);
            addSubscription.setBundleId(bundleEntity.getBundleId());
            addSubscription.setSubscriptionId(UUID.randomUUID().toString());
            addSubscription.setProductName(plan.getProduct());
            addSubscription.setProductCategory(plan.getProductCategory());
            addSubscription.setPlanId(plan.getId());
            addSubscription.setMetadata(data.getRemark());
            addSubscription.setState(SubscriptionState.ACTIVATED.name());
            addSubscription.setBundleStartDate(startDate);
            addSubscription.setStartDate(startDate);
            addSubscription.setExpireDate(endDate);
            addSubscription.setCreatedBy(createdBy);
            addSubscription.setUpdatedBy(createdBy);
            entities.add(addSubscription);

            iSubscriptionService.createBatch(entities);
            return;
        }
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setSpaceId(spaceId);
        subscription.setBundleId(activeBundle.getBundleId());
        subscription.setSubscriptionId(UUID.randomUUID().toString());
        subscription.setProductName(plan.getProduct());
        Product product =
            BillingConfigManager.getBillingConfig().getProducts().get(plan.getProduct());
        subscription.setProductCategory(product.getCategory());
        subscription.setPlanId(plan.getId());
        subscription.setMetadata(data.getRemark());
        subscription.setState(SubscriptionState.ACTIVATED.name());
        subscription.setBundleStartDate(activeBundle.getBundleStartDate());
        subscription.setStartDate(startDate);
        subscription.setExpireDate(endDate);
        subscription.setCreatedBy(createdBy);
        subscription.setUpdatedBy(createdBy);
        iSubscriptionService.create(subscription);
    }

    private BundleEntity createBundle(String spaceId, LocalDateTime startDate,
                                      LocalDateTime endDate, Long createdBy) {
        BundleEntity bundleEntity = new BundleEntity();
        bundleEntity.setBundleId(UUID.randomUUID().toString());
        bundleEntity.setSpaceId(spaceId);
        bundleEntity.setState(ACTIVATED.name());
        bundleEntity.setStartDate(startDate);
        bundleEntity.setEndDate(endDate);
        bundleEntity.setCreatedBy(createdBy);
        bundleEntity.setUpdatedBy(createdBy);
        return bundleEntity;
    }

    private boolean isSocialBind(String spaceId) {
        TenantBindDTO bindDTO = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (bindDTO == null) {
            return false;
        }
        SocialTenantEntity tenant =
            iSocialTenantService.getByAppIdAndTenantId(bindDTO.getAppId(), bindDTO.getTenantId());
        if (tenant == null) {
            return false;
        }
        return Boolean.TRUE.equals(tenant.getStatus()) &&
            SocialAppType.ISV.getType() == tenant.getAppType();
    }
}
