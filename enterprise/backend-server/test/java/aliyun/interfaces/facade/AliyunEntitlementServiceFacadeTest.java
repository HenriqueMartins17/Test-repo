package com.apitable.enterprise.aliyun.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractAliyunIntegrationTest;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import org.junit.jupiter.api.Test;

public class AliyunEntitlementServiceFacadeTest extends AbstractAliyunIntegrationTest {

    @Test
    public void testFreeEntitlement() {
        SubscriptionInfo subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscription("");
        assertThat(subscriptionInfo).isNotNull();
        assertThat(subscriptionInfo.getProduct()).isEqualTo("Atlas");
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo("atlas_unlimited");
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
