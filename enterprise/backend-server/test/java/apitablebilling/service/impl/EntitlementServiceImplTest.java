package com.apitable.enterprise.apitablebilling.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import com.apitable.enterprise.stripe.config.Price;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

public class EntitlementServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    public void testGetFreeEntitlement() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(ProductEnum.FREE.getName());
        assertThat(subscriptionInfo.getBillingMode()).isNullOrEmpty();
        assertThat(subscriptionInfo.getRecurringInterval()).isNullOrEmpty();
        int cycleDayOfMonth = subscriptionInfo.cycleDayOfMonth(10);
        assertThat(cycleDayOfMonth).isEqualTo(10);
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isFalse();
        assertThat(seat.getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getFileNodeNums().getValue()).isEqualTo(20);
        assertThat(subscriptionInfo.getFeature().getRowsPerSheet().getValue()).isEqualTo(100);
        assertThat(subscriptionInfo.getFeature().getColumnsPerSheet().getValue()).isEqualTo(30);
        assertThat(
            subscriptionInfo.getFeature().getCapacitySize().getValue().toGigabytes()).isEqualTo(1);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(20);
        assertThat(subscriptionInfo.getFeature().getAiAgentNums().getValue()).isEqualTo(1);
        assertThat(subscriptionInfo.getFeature().getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getArchitectureViewNums().getValue()).isEqualTo(
            -1);
        assertThat(subscriptionInfo.getFeature().getGanttViewNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getCalendarViewNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getFormNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getMirrorNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getWidgetNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getDashboardNums().getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getAllowEmbed().getValue()).isFalse();
        assertThat(subscriptionInfo.getFeature().getControlFormBrandLogo().getValue()).isFalse();
        assertThat(subscriptionInfo.getFeature().getRainbowLabel().getValue()).isFalse();
        assertThat(
            subscriptionInfo.getFeature().getRemainRecordActivityDays().getValue()).isEqualTo(14);
        assertThat(subscriptionInfo.getFeature().getSnapshotNumsPerSheet().getValue()).isEqualTo(1);
        assertThat(subscriptionInfo.getFeature().getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(
            subscriptionInfo.getFeature().getAutomationRunNumsPerMonth().getValue()).isEqualTo(30);
        assertThat(subscriptionInfo.getFeature().getNodePermissionNums().getValue()).isEqualTo(1);
        assertThat(subscriptionInfo.getFeature().getFieldPermissionNums().getValue()).isEqualTo(1);
    }

    @Test
    public void testGetStarterEntitlement() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum starter = ProductEnum.STARTER;
        Price price = findPrice(starter, RecurringInterval.MONTH);
        MockUserSpace userSpace = createEntitlement(starter, price.getId(), startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 12, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(starter.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        int cycleDayOfMonth = subscriptionInfo.cycleDayOfMonth(10);
        assertThat(cycleDayOfMonth).isEqualTo(3);
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isFalse();
        assertThat(seat.getValue()).isEqualTo(2);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            1000L);

        final OffsetDateTime nowDate =
            OffsetDateTime.of(2023, 2, 4, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(nowDate);

        SubscriptionInfo currentSubscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(currentSubscriptionInfo.getProduct()).isEqualTo(ProductEnum.FREE.getName());

        cycleDayOfMonth = currentSubscriptionInfo.cycleDayOfMonth(nowDate.getDayOfMonth());
        assertThat(cycleDayOfMonth).isEqualTo(nowDate.getDayOfMonth());
    }

    @Test
    public void testGetLegacyPlusEntitlement() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum plus = ProductEnum.PLUS;
        MockUserSpace userSpace = createEntitlement(plus, "not-exsit", startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(plus.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(1);
        var aiAgentNums = subscriptionInfo.getFeature().getAiAgentNums();
        assertThat(aiAgentNums.getValue()).isEqualTo(3);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            500L);
    }

    @Test
    public void testGetPlusEntitlement() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum plus = ProductEnum.PLUS;
        Price price = findPrice(plus, RecurringInterval.MONTH);
        MockUserSpace userSpace = createEntitlement(plus, price.getId(), startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(plus.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(-1);
        var aiAgentNums = subscriptionInfo.getFeature().getAiAgentNums();
        assertThat(aiAgentNums.getValue()).isEqualTo(3);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            2000L);
    }

    @Test
    public void testGetPlusEntitlementWithExpirePrice() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum plus = ProductEnum.PLUS;
        MockUserSpace userSpace = createEntitlement(plus, "expire-price-id", startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(plus.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(1);
        var aiAgentNums = subscriptionInfo.getFeature().getAiAgentNums();
        assertThat(aiAgentNums.getValue()).isEqualTo(3);
        assertThat(subscriptionInfo.getFeature().getAdminNums().getValue()).isEqualTo(5);
        assertThat(subscriptionInfo.getFeature().getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(subscriptionInfo.getFeature().getRowsPerSheet().getValue()).isEqualTo(10000);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            500L);
        assertThat(subscriptionInfo.getFeature().getAuditQuery().getValue()).isFalse();
    }


    @Test
    public void testGetProEntitlement() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum pro = ProductEnum.PRO;
        Price price = findPrice(pro, RecurringInterval.MONTH);
        MockUserSpace userSpace = createEntitlement(pro, price.getId(), startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(pro.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            5000L);
    }

    @Test
    public void testGetProEntitlementWithExpirePrice() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum pro = ProductEnum.PRO;
        MockUserSpace userSpace = createEntitlement(pro, "expire-price-id", startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(pro.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(1);
        var aiAgentNums = subscriptionInfo.getFeature().getAiAgentNums();
        assertThat(aiAgentNums.getValue()).isEqualTo(30);
        assertThat(subscriptionInfo.getFeature().getAdminNums().getValue()).isEqualTo(10);
        assertThat(subscriptionInfo.getFeature().getFileNodeNums().getValue()).isEqualTo(1000);
        assertThat(subscriptionInfo.getFeature().getRowsPerSheet().getValue()).isEqualTo(20000);
        assertThat(subscriptionInfo.getFeature().getApiCallNumsPerMonth().getValue()).isEqualTo(
            500000);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            1200);
        assertThat(subscriptionInfo.getFeature().getAuditQuery().getValue()).isFalse();
    }

    @Test
    public void testGetBusinessEntitlement() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum business = ProductEnum.BUSINESS;
        Price price = findPrice(business, RecurringInterval.MONTH);
        MockUserSpace userSpace = createEntitlement(business, price.getId(), startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(ProductEnum.BUSINESS.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            10000L);
    }

    @Test
    public void testGetEnterpriseEntitlementWithExpirePrice() {
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 3, 10, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        ProductEnum enterprise = ProductEnum.ENTERPRISE;
        MockUserSpace userSpace =
            createEntitlement(enterprise, "expire-price-id", startDate, endDate);

        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 1, 3, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo(enterprise.getName());
        assertThat(subscriptionInfo.getRecurringInterval()).isEqualTo(
            BillingPeriod.MONTHLY.getName());
        assertThat(subscriptionInfo.getStartDate()).isEqualTo(startDate.toLocalDate());
        assertThat(subscriptionInfo.getEndDate()).isEqualTo(endDate.toLocalDate());
        assertThat(subscriptionInfo.getFeature()).isNotNull();
        var seat = subscriptionInfo.getFeature().getSeat();
        assertThat(seat.isUnlimited()).isTrue();
        assertThat(seat.getValue()).isEqualTo(1);
        var aiAgentNums = subscriptionInfo.getFeature().getAiAgentNums();
        assertThat(aiAgentNums.getValue()).isEqualTo(100);
        assertThat(subscriptionInfo.getFeature().getAdminNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getFileNodeNums().getValue()).isEqualTo(10000);
        assertThat(subscriptionInfo.getFeature().getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(subscriptionInfo.getFeature().getTotalRows().getValue()).isEqualTo(500000000);
        assertThat(subscriptionInfo.getFeature().getFormNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getWidgetNums().getValue()).isEqualTo(-1);
        assertThat(
            subscriptionInfo.getFeature().getAutomationRunNumsPerMonth().getValue()).isEqualTo(
            500000);
        assertThat(subscriptionInfo.getFeature().getNodePermissionNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getFieldPermissionNums().getValue()).isEqualTo(-1);
        assertThat(subscriptionInfo.getFeature().getApiCallNumsPerMonth().getValue()).isEqualTo(
            1000000L);
        assertThat(subscriptionInfo.getFeature().getMessageCreditNums().getValue()).isEqualTo(
            3000);
        assertThat(subscriptionInfo.getFeature().getWatermark().getValue()).isTrue();
        assertThat(subscriptionInfo.getFeature().getAuditQuery().getValue()).isTrue();
    }
}
