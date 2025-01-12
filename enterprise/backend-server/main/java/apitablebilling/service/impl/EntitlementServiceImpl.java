package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.apitablebilling.util.BillingUtil.buildFeature;
import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.findPrice;
import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.getProductFeature;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.core.Bundle;
import com.apitable.enterprise.apitablebilling.core.Subscription;
import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.StripeCustomerEntity;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingMode;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.apitablebilling.model.dto.EntitlementCreationDTO;
import com.apitable.enterprise.apitablebilling.rewardful.RewardfulService;
import com.apitable.enterprise.apitablebilling.rewardful.model.RewardfulData;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.apitablebilling.service.IStripeCustomerService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.enterprise.stripe.core.model.SubscriptionCreation;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.stripe.model.Customer;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * entitlement service implements.
 */
@Slf4j
@Service
public class EntitlementServiceImpl implements IEntitlementService {

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private IStripeCustomerService iStripeCustomerService;

    @Resource
    private IUserService iUserService;

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Resource
    private RewardfulService rewardfulService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createEntitlementWithTrial(String spaceId, Long createdBy,
                                           Map<String, String> externalProperty) {
        log.info("create entitlement with trial, spaceId: {}", spaceId);
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        // find user
        UserEntity userEntity = iUserService.getById(createdBy);
        // find customer
        List<StripeCustomerEntity> stripeCustomers = iStripeCustomerService.getBySpaceId(spaceId);
        Customer customer;
        if (stripeCustomers.isEmpty()) {
            // create customer in stripe
            customer = stripeTemplate.createCustomer(userEntity.getNickName(),
                userEntity.getEmail(), rewardfulData.getCustomerMetadata(),
                rewardfulData.getCoupon());

            // save customer in db
            iStripeCustomerService.createCustomer(customer.getId(), userEntity.getEmail(), spaceId);
        } else {
            // retrieve customer in stripe
            StripeCustomerEntity stripeCustomer = stripeCustomers.get(0);
            customer = stripeTemplate.updateCustomerIfSatisfy(stripeCustomer.getStripeId(),
                rewardfulData.getCustomerMetadata(), rewardfulData.getCoupon());
        }

        // create subscription
        try {
            Map<String, String> metadata = new HashMap<>(1);
            metadata.put("spaceId", spaceId);
            stripeTemplate.createSubscriptionWithTrialPeriod(
                SubscriptionCreation.builder()
                    .stripeCustomerId(customer.getId())
                    .trialPeriodDays(14L)
                    .metadata(metadata)
                    .build()
            );
        } catch (Exception exception) {
            // rollback customer create in stripe
            stripeTemplate.deleteCustomer(customer.getId());
            throw new RuntimeException("fail to create trial subscription", exception);
        }
    }

    @Override
    public void createEntitlement(EntitlementCreationDTO entitlementCreationDTO) {
        // Create a space station subscription bundle
        BundleEntity bundleEntity = new BundleEntity();
        bundleEntity.setBundleId(UUID.randomUUID().toString());
        bundleEntity.setSpaceId(entitlementCreationDTO.getSpaceId());
        bundleEntity.setState(entitlementCreationDTO.isTrial() ? BundleState.TRIALING.name() :
            BundleState.ACTIVATED.name());
        bundleEntity.setStartDate(entitlementCreationDTO.getStartDate());
        bundleEntity.setEndDate(entitlementCreationDTO.getEndDate());

        final List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        // Create base type subscription
        String subscriptionId = UUID.randomUUID().toString();
        SubscriptionEntity base = new SubscriptionEntity();
        base.setSpaceId(entitlementCreationDTO.getSpaceId());
        base.setBundleId(bundleEntity.getBundleId());
        base.setSubscriptionId(subscriptionId);
        base.setStripeId(entitlementCreationDTO.getStripeId());
        base.setStripeSubId(entitlementCreationDTO.getStripeSubId());
        base.setProductName(entitlementCreationDTO.getProductName());
        ProductEnum productEnum = ProductEnum.of(entitlementCreationDTO.getProductName());
        base.setProductCategory(productEnum.getCategory().name());
        base.setPriceId(entitlementCreationDTO.getPriceId());
        base.setPeriod(entitlementCreationDTO.getPeriod().getName());
        base.setQuantity(entitlementCreationDTO.getQuantity());
        base.setState(entitlementCreationDTO.isTrial() ? SubscriptionState.TRIALING.name() :
            SubscriptionState.ACTIVATED.name());
        base.setBundleStartDate(entitlementCreationDTO.getStartDate());
        base.setStartDate(entitlementCreationDTO.getStartDate());
        base.setExpireDate(entitlementCreationDTO.getEndDate());
        subscriptionEntities.add(base);

        iBundleInApitableService.create(bundleEntity);
        iSubscriptionInApitableService.createBatch(subscriptionEntities);
    }

    @Override
    public SubscriptionInfo getEntitlementBySpaceId(String spaceId) {
        Bundle bundle = iBundleInApitableService.getValidBundleBySpaceId(spaceId);
        if (bundle == null) {
            // Return the free subscription plan
            return new BillingSubscriptionInfo();
        }
        // Basic subscription
        Subscription baseSubscription = bundle.getBaseSubscription();
        LocalDate baseExpireDate = baseSubscription.getExpireDate().toLocalDate();
        LocalDate now = ClockManager.me().getLocalDateNow();
        boolean isBaseEntitlementExpire =
            bundle.isBaseForFree() || now.isAfter(baseExpireDate);
        LocalDate endDate = isBaseEntitlementExpire ? null : baseExpireDate;
        ProductEnum productEnum = ProductEnum.of(baseSubscription.getProductName());
        boolean isTrialing = baseSubscription.getState() == SubscriptionState.TRIALING;
        LocalDate startDate = bundle.getBundleStartDate().toLocalDate();
        // compatible with old plan
        boolean found =
            findPrice(productEnum, baseSubscription.getPriceId());
        if (!found) {
            BillingSubscriptionInfo subscriptionInfo = new BillingSubscriptionInfo(
                bundle,
                productEnum,
                isTrialing,
                startDate,
                endDate,
                buildFeature(productEnum, baseSubscription.getQuantity(), isTrialing)
            );
            subscriptionInfo.setBillingMode(BillingMode.RECURRING.getName());
            subscriptionInfo.setRecurringInterval(baseSubscription.getPeriod().getName());
            return subscriptionInfo;
        }
        BillingSubscriptionInfo subscriptionInfo = new BillingSubscriptionInfo(
            bundle,
            productEnum,
            isTrialing,
            startDate,
            endDate,
            getProductFeature(productEnum).safeConvert()
        );
        subscriptionInfo.setBillingMode(BillingMode.RECURRING.getName());
        subscriptionInfo.setRecurringInterval(baseSubscription.getPeriod().getName());
        return subscriptionInfo;
    }

    @Override
    public Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(
        List<String> spaceIds) {
        List<Bundle> bundles = iBundleInApitableService.getValidBundlesBySpaceId(spaceIds);
        Map<String, List<Bundle>> bundleMap = bundles.stream()
            .collect(Collectors.groupingBy(Bundle::getSpaceId));
        Map<String, SubscriptionFeature> planFeatureMap = new HashMap<>(spaceIds.size());
        for (String spaceId : spaceIds) {
            if (bundleMap.containsKey(spaceId)) {
                Subscription baseSubscription =
                    bundleMap.get(spaceId).iterator().next().getBaseSubscription();
                boolean isTrialing = baseSubscription.getState() == SubscriptionState.TRIALING;
                ProductEnum productEnum = ProductEnum.of(baseSubscription.getProductName());
                boolean found =
                    findPrice(productEnum, baseSubscription.getPriceId());
                SubscriptionFeature feature = !found ?
                    buildFeature(productEnum, baseSubscription.getQuantity(), isTrialing)
                    : getProductFeature(productEnum).safeConvert();
                planFeatureMap.put(spaceId, feature);
            } else {
                BillingSubscriptionInfo freeSubscriptionInfo =
                    new BillingSubscriptionInfo();
                planFeatureMap.put(spaceId, freeSubscriptionInfo.getFeature());
            }
        }
        return planFeatureMap;
    }

    @Override
    public void cancelSubscription(String spaceId) {
        Bundle bundle = iBundleInApitableService.getValidBundleBySpaceId(spaceId);
        if (bundle == null) {
            return;
        }
        Subscription baseSubscription = bundle.getBaseSubscription();
        if (baseSubscription == null) {
            return;
        }
        String stripeId = baseSubscription.getStripeId();
        if (StrUtil.isBlank(stripeId)) {
            return;
        }
        stripeTemplate.cancelSubscription(stripeId);
    }
}
