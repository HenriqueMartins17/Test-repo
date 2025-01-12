package com.apitable.enterprise.selfhostbilling.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractSelfHostedIntegrationTest;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class SelfHostedEntitlementServiceFacadeTest extends AbstractSelfHostedIntegrationTest {

    @Test
    void testGetSpaceSubscription() {
        String spaceId = "spaceId";
        SubscriptionInfo subscriptionInfo = entitlementServiceFacade.getSpaceSubscription(spaceId);
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo("Private_Cloud");
        assertThat(subscriptionInfo.getStartDate()).isNull();
        assertThat(subscriptionInfo.getEndDate().toString()).isEqualTo("2030-01-20");
        SubscriptionFeature feature = subscriptionInfo.getFeature();
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getCapacitySize().isUnlimited()).isTrue();
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(-1);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(500000000);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(-1);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(-1);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(-1);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getFormNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(-1);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(-1);
        assertThat(feature.getSocialConnect().getValue()).isEqualTo(false);
        assertThat(feature.getRainbowLabel().getValue()).isEqualTo(true);
        assertThat(feature.getWatermark().getValue()).isEqualTo(true);
        assertThat(feature.getAllowInvitation().getValue()).isEqualTo(true);
        assertThat(feature.getAllowApplyJoin().getValue()).isEqualTo(true);
        assertThat(feature.getAllowShare().getValue()).isEqualTo(true);
        assertThat(feature.getAllowExport().getValue()).isEqualTo(true);
        assertThat(feature.getAllowDownload().getValue()).isEqualTo(true);
        assertThat(feature.getAllowCopyData().getValue()).isEqualTo(true);
        assertThat(feature.getAllowEmbed().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
        assertThat(feature.getShowMobileNumber().getValue()).isEqualTo(true);
        assertThat(feature.getContactIsolation().getValue()).isEqualTo(true);
        assertThat(feature.getForbidCreateOnCatalog().getValue()).isEqualTo(true);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(730);
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(730);
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(730);
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(730L);
    }
}
