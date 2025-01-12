package com.apitable.enterprise.selfhostbilling.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.license.model.LicenseInfo;
import com.apitable.enterprise.license.service.LicenseService;
import com.apitable.enterprise.selfhostbilling.interfaces.model.SelfhostSubscriptionInfo;
import com.apitable.enterprise.selfhostbilling.service.ISelfhostEntitlementService;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.clock.spring.ClockManager;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

import static com.apitable.shared.exception.LimitException.FORBIDDEN_ACCESS;


/**
 * entitlement service implements.
 *
 * @author Shawn Deng
 */
@Service
public class SelfhostEntitlementServiceImpl implements ISelfhostEntitlementService {

    @Resource
    private RedisOperations<String, Object> redisOperations;

    @Value("${spring.session.redis.namespace:}")
    private String sessionNamespace;

    @Value("${selfhost.license:}")
    private String selfHostLicense;

    @Resource
    private LicenseService licenseService;

    private LicenseInfo licenseInfo;

    /**
     * init license
     */
    @PostConstruct
    public void init(){
        if (!selfHostLicense.isEmpty()) {
            licenseService.saveOrUpdateLicense(selfHostLicense);
        }
        licenseInfo = licenseService.getCurrentLicense();
    }

    @Override
    public SubscriptionInfo getEntitlementBySpaceId(String spaceId) {
        if (licenseInfo == null ) {
            throw new RuntimeException("The license is invalid or has expired.");
        }
        // Conversion license expiration time
        Instant instant = Instant.ofEpochMilli(licenseInfo.getLicense().getExpireAt());
        LocalDate endDate = instant.atZone(ClockManager.me().getDefaultTimeZone()).toLocalDate();
        LocalDate now = ClockManager.me().getLocalDateNow();
        if (now.isAfter(endDate)) {
            // Get all session keys
            Set<String> keys = redisOperations.opsForValue().getOperations().keys(sessionNamespace + ":sessions:*");
            // Delete all session
            redisOperations.delete(keys);
            throw new BusinessException(FORBIDDEN_ACCESS);
        }
        return new SelfhostSubscriptionInfo(endDate);
    }

    @Override
    public Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(
            List<String> spaceIds) {
        Map<String, SubscriptionFeature> planFeatureMap = new HashMap<>(spaceIds.size());
        spaceIds.forEach(spaceId -> {
            SubscriptionInfo subscriptionInfo = getEntitlementBySpaceId(spaceId);
            planFeatureMap.put(spaceId, subscriptionInfo.getFeature());
        });
        return planFeatureMap;
    }

}
