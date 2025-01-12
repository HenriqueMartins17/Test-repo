package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.findPrice;

import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingSchema;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionReportType;
import com.apitable.enterprise.apitablebilling.service.IBillingTaskService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionReportHistoryService;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SeatUsage;
import com.stripe.model.Price;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * billing task service implement.
 *
 * @author Shawn Deng
 */
@Service
@Slf4j
public class BillingTaskServiceImpl implements IBillingTaskService {

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private ISubscriptionReportHistoryService iSubscriptionReportHistoryService;

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String KEY = "apitable:billing:report:ignored:spaces";

    private static final int TIMEOUT = 2;

    private List<String> getIgnoredSpaces() {
        BoundListOperations<String, String> opts = redisTemplate.boundListOps(KEY);
        return opts.range(0, Optional.ofNullable(opts.size()).orElse(1L));
    }

    private void addIgnoredSpace(String spaceId) {
        BoundListOperations<String, String> opts = redisTemplate.boundListOps(KEY);
        opts.leftPush(spaceId);
        opts.expire(TIMEOUT, TimeUnit.HOURS);
    }

    private Long findSpaceId(String spaceId) {
        BoundListOperations<String, String> opts = redisTemplate.boundListOps(KEY);
        Long index = opts.indexOf(spaceId);
        return Optional.ofNullable(index).orElse(-1L);
    }

    private void removeIf(String spaceId) {
        BoundListOperations<String, String> opts = redisTemplate.boundListOps(KEY);
        Long index = findSpaceId(spaceId);
        if (index != -1L) {
            opts.remove(1, spaceId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportSpaceSubscriptionSeats() {
        LocalDate now = ClockManager.me().getLocalDateNow();
        // query space which has activated subscription, handle ten at most one time
        List<SubscriptionEntity> activatedSubscriptionEntities =
            iSubscriptionInApitableService.getValidSubscriptions();
        if (activatedSubscriptionEntities.isEmpty()) {
            return;
        }
        // compare every space member total count, report if difference
        for (SubscriptionEntity item : activatedSubscriptionEntities) {
            SpaceEntity spaceEntity = iSpaceService.getEntityBySpaceId(item.getSpaceId());
            if (spaceEntity == null) {
                continue;
            }
            ProductEnum productEnum = ProductEnum.of(item.getProductName());
            boolean found =
                findPrice(productEnum, item.getPriceId());
            if (found) {
                // need to report to stripe if price is not found
                log.info(
                    "the subscription does not need report, subscription id: {}, price id: {}, stripe id: {}-{}",
                    item.getSubscriptionId(), item.getPriceId(), item.getStripeId(),
                    item.getStripeSubId());
                continue;
            }
            Price stripePrice =
                stripeTemplate.retrievePrice(item.getPriceId(), "tiers");
            BillingSchema schema = BillingSchema.of(stripePrice.getBillingScheme());
            if (schema == null) {
                throw new RuntimeException(
                    String.format("price %s schema %s can not determine",
                        item.getPriceId(), stripePrice.getBillingScheme()));
            }
            if (schema.isTieredSchema()) {
                tieredReport(item, stripePrice, now);
            }
            if (schema.isPerUnitSchema()) {
                perUnitReport(item, now);
            }
        }
    }

    private void tieredReport(SubscriptionEntity item, Price stripePrice, LocalDate reportedDate) {
        long firstTierUnit = stripePrice.getTiers().iterator().next().getUpTo();
        // current billing quantity
        long subQuantity =
            stripeTemplate.getFirstSubscriptionItemQuantity(item.getStripeId());
        // current space member count
        SeatUsage seatUsage = iSpaceService.getSeatUsage(item.getSpaceId());
        if (subQuantity != seatUsage.getMemberCount()) {
            long reportedUnit = 0;
            if (subQuantity > firstTierUnit) {
                // billing quantity is greater than price include unit
                // report unit
                reportedUnit = Math.max(seatUsage.getMemberCount(), firstTierUnit);
            } else {
                if (seatUsage.getMemberCount() > firstTierUnit) {
                    // report unit
                    reportedUnit = seatUsage.getMemberCount();
                }
            }
            if (reportedUnit != 0) {
                // update subscription through stripe api
                stripeTemplate.updateSubscriptionQuantity(item.getStripeId(),
                    reportedUnit);
                //remember reported space, avoid report again next time
                iSubscriptionReportHistoryService.create(item.getSubscriptionId(),
                    SubscriptionReportType.QUANTITY, reportedDate);
            }
        }
    }

    private void perUnitReport(SubscriptionEntity item, LocalDate reportedDate) {
        // current space member count
        SeatUsage seatUsage = iSpaceService.getSeatUsage(item.getSpaceId());
        List<String> ignoredSpaces = getIgnoredSpaces();
        if (seatUsage.getMemberCount() == 0L) {
            // filter ignored space
            if (!ignoredSpaces.contains(item.getSpaceId())) {
                addIgnoredSpace(item.getSpaceId());
                log.error("member counts of space {} can not be zero", item.getSpaceId());
            }
            return;
        }
        removeIf(item.getSpaceId());
        // current billing quantity
        long subQuantity =
            stripeTemplate.getFirstSubscriptionItemQuantity(item.getStripeId());
        if (subQuantity != seatUsage.getMemberCount()) {
            // update subscription through stripe api
            stripeTemplate.updateSubscriptionQuantity(item.getStripeId(),
                seatUsage.getMemberCount());
            //remember reported space, avoid report again next time
            iSubscriptionReportHistoryService.create(item.getSubscriptionId(),
                SubscriptionReportType.QUANTITY, reportedDate);
        }
    }

}
