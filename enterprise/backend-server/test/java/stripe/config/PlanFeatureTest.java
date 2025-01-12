package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import org.junit.jupiter.api.Test;

public class PlanFeatureTest {

    @Test
    void testFreePlanFeature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.FREE);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(2);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(20);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(100);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(250);
        assertThat(feature.getColumnsPerSheet().getValue()).isEqualTo(30);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(20);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(1);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getArchitectureViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(2);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(2);
        assertThat(feature.getFormNums().getValue()).isEqualTo(2);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(2);
        assertThat(feature.getWidgetNums().getValue()).isEqualTo(2);
        assertThat(feature.getDashboardNums().getValue()).isEqualTo(2);
        assertThat(feature.getEmbedding().getValue()).isFalse();
        assertThat(feature.getRainbowLabel().getValue()).isFalse();
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(14);
        assertThat(feature.getSnapshotNumsPerSheet().getValue()).isEqualTo(1);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(30);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(60);
        assertThat(feature.getApiQpsNums().getValue()).isEqualTo(2);
        assertThat(feature.getOrgApi().getValue()).isFalse();
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(1);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(1);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(1);
        assertThat(feature.getWatermark().getValue()).isFalse();
        assertThat(feature.getDataExport().getValue()).isFalse();
        assertThat(feature.getNodeShare().getValue()).isFalse();
        assertThat(feature.getDataCopy().getValue()).isFalse();
        assertThat(feature.getAttachmentDownload().getValue()).isFalse();
        assertThat(feature.getInvitationOff().getValue()).isFalse();
        assertThat(feature.getApplyJoinOff().getValue()).isFalse();
        assertThat(feature.getControlOperationInWorkbench().getValue()).isFalse();
        assertThat(feature.getContactIsolation().getValue()).isFalse();
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(0);
        assertThat(feature.getAuditQuery().getValue()).isFalse();
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(false);

        assertThat(feature.getArchivedRowsPerSheet().getValue()).isEqualTo(100);
        assertThat(feature.getSocialConnect().getValue()).isFalse();
        assertThat(feature.getMobileDisplayable().getValue()).isFalse();
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(1);
    }

    @Test
    void testStarterFeature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.STARTER);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(2);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(10000);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(100000);
        assertThat(feature.getColumnsPerSheet().getValue()).isEqualTo(60);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(20);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(2);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getArchitectureViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(10);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(10);
        assertThat(feature.getFormNums().getValue()).isEqualTo(10);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(5);
        assertThat(feature.getWidgetNums().getValue()).isEqualTo(5);
        assertThat(feature.getDashboardNums().getValue()).isEqualTo(5);
        assertThat(feature.getEmbedding().getValue()).isFalse();
        assertThat(feature.getRainbowLabel().getValue()).isTrue();
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(30);
        assertThat(feature.getSnapshotNumsPerSheet().getValue()).isEqualTo(2);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(30);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(500);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(500);
        assertThat(feature.getApiQpsNums().getValue()).isEqualTo(5);
        assertThat(feature.getOrgApi().getValue()).isFalse();
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(5);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(5);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(1);
        assertThat(feature.getWatermark().getValue()).isFalse();
        assertThat(feature.getDataExport().getValue()).isFalse();
        assertThat(feature.getNodeShare().getValue()).isFalse();
        assertThat(feature.getDataCopy().getValue()).isFalse();
        assertThat(feature.getAttachmentDownload().getValue()).isFalse();
        assertThat(feature.getInvitationOff().getValue()).isFalse();
        assertThat(feature.getApplyJoinOff().getValue()).isFalse();
        assertThat(feature.getControlOperationInWorkbench().getValue()).isFalse();
        assertThat(feature.getContactIsolation().getValue()).isFalse();
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(0);
        assertThat(feature.getAuditQuery().getValue()).isFalse();
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(false);

        assertThat(feature.getArchivedRowsPerSheet().getValue()).isEqualTo(1000);
        assertThat(feature.getSocialConnect().getValue()).isFalse();
        assertThat(feature.getMobileDisplayable().getValue()).isFalse();
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(14);
    }

    @Test
    void testPlusFeature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.PLUS);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(600);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(20000);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(200000);
        assertThat(feature.getColumnsPerSheet().getValue()).isEqualTo(80);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(50);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(2000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(3);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getArchitectureViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(20);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(20);
        assertThat(feature.getFormNums().getValue()).isEqualTo(50);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(50);
        assertThat(feature.getWidgetNums().getValue()).isEqualTo(20);
        assertThat(feature.getDashboardNums().getValue()).isEqualTo(50);
        assertThat(feature.getEmbedding().getValue()).isFalse();
        assertThat(feature.getRainbowLabel().getValue()).isTrue();
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(90);
        assertThat(feature.getSnapshotNumsPerSheet().getValue()).isEqualTo(5);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(90);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getApiQpsNums().getValue()).isEqualTo(5);
        assertThat(feature.getOrgApi().getValue()).isFalse();
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(50);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(50);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(2);
        assertThat(feature.getWatermark().getValue()).isFalse();
        assertThat(feature.getDataExport().getValue()).isFalse();
        assertThat(feature.getNodeShare().getValue()).isFalse();
        assertThat(feature.getDataCopy().getValue()).isFalse();
        assertThat(feature.getAttachmentDownload().getValue()).isFalse();
        assertThat(feature.getInvitationOff().getValue()).isFalse();
        assertThat(feature.getApplyJoinOff().getValue()).isFalse();
        assertThat(feature.getControlOperationInWorkbench().getValue()).isFalse();
        assertThat(feature.getContactIsolation().getValue()).isFalse();
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(0);
        assertThat(feature.getAuditQuery().getValue()).isFalse();
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(false);

        assertThat(feature.getArchivedRowsPerSheet().getValue()).isEqualTo(3000);
        assertThat(feature.getSocialConnect().getValue()).isFalse();
        assertThat(feature.getMobileDisplayable().getValue()).isFalse();
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(90);
    }

    @Test
    void testProFeature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.PRO);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(3000);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(500000);
        assertThat(feature.getColumnsPerSheet().getValue()).isEqualTo(120);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(500);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(5000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(30);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getArchitectureViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(50);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(50);
        assertThat(feature.getFormNums().getValue()).isEqualTo(100);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(100);
        assertThat(feature.getWidgetNums().getValue()).isEqualTo(150);
        assertThat(feature.getDashboardNums().getValue()).isEqualTo(50);
        assertThat(feature.getEmbedding().getValue()).isTrue();
        assertThat(feature.getRainbowLabel().getValue()).isTrue();
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(180);
        assertThat(feature.getSnapshotNumsPerSheet().getValue()).isEqualTo(50);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(180);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(feature.getApiQpsNums().getValue()).isEqualTo(10);
        assertThat(feature.getOrgApi().getValue()).isFalse();
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(200);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(200);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(5);
        assertThat(feature.getWatermark().getValue()).isFalse();
        assertThat(feature.getDataExport().getValue()).isTrue();
        assertThat(feature.getNodeShare().getValue()).isFalse();
        assertThat(feature.getDataCopy().getValue()).isFalse();
        assertThat(feature.getAttachmentDownload().getValue()).isFalse();
        assertThat(feature.getInvitationOff().getValue()).isFalse();
        assertThat(feature.getApplyJoinOff().getValue()).isFalse();
        assertThat(feature.getControlOperationInWorkbench().getValue()).isFalse();
        assertThat(feature.getContactIsolation().getValue()).isFalse();
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(0);
        assertThat(feature.getAuditQuery().getValue()).isFalse();
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);

        assertThat(feature.getArchivedRowsPerSheet().getValue()).isEqualTo(20000);
        assertThat(feature.getSocialConnect().getValue()).isFalse();
        assertThat(feature.getMobileDisplayable().getValue()).isFalse();
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(180);
    }

    @Test
    void testBusinessFeature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.BUSINESS);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(10000);
        assertThat(feature.getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(feature.getTotalRows().getValue()).isEqualTo(1000000);
        assertThat(feature.getColumnsPerSheet().getValue()).isEqualTo(150);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1000);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(100);
        assertThat(feature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getArchitectureViewNums().getValue()).isEqualTo(-1);
        assertThat(feature.getGanttViewNums().getValue()).isEqualTo(200);
        assertThat(feature.getCalendarViewNums().getValue()).isEqualTo(200);
        assertThat(feature.getFormNums().getValue()).isEqualTo(300);
        assertThat(feature.getMirrorNums().getValue()).isEqualTo(300);
        assertThat(feature.getWidgetNums().getValue()).isEqualTo(300);
        assertThat(feature.getDashboardNums().getValue()).isEqualTo(100);
        assertThat(feature.getEmbedding().getValue()).isTrue();
        assertThat(feature.getRainbowLabel().getValue()).isTrue();
        assertThat(feature.getRemainRecordActivityDays().getValue()).isEqualTo(360);
        assertThat(feature.getSnapshotNumsPerSheet().getValue()).isEqualTo(100);
        assertThat(feature.getRemainTrashDays().getValue()).isEqualTo(720);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getApiQpsNums().getValue()).isEqualTo(20);
        assertThat(feature.getOrgApi().getValue()).isTrue();
        assertThat(feature.getFieldPermissionNums().getValue()).isEqualTo(500);
        assertThat(feature.getNodePermissionNums().getValue()).isEqualTo(500);
        assertThat(feature.getAdminNums().getValue()).isEqualTo(20);
        assertThat(feature.getWatermark().getValue()).isTrue();
        assertThat(feature.getDataExport().getValue()).isTrue();
        assertThat(feature.getNodeShare().getValue()).isTrue();
        assertThat(feature.getDataCopy().getValue()).isTrue();
        assertThat(feature.getAttachmentDownload().getValue()).isFalse();
        assertThat(feature.getInvitationOff().getValue()).isFalse();
        assertThat(feature.getApplyJoinOff().getValue()).isFalse();
        assertThat(feature.getControlOperationInWorkbench().getValue()).isFalse();
        assertThat(feature.getContactIsolation().getValue()).isFalse();
        assertThat(feature.getAuditQueryDays().getValue()).isEqualTo(-1);
        assertThat(feature.getAuditQuery().getValue()).isTrue();
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);

        assertThat(feature.getArchivedRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(feature.getSocialConnect().getValue()).isFalse();
        assertThat(feature.getMobileDisplayable().getValue()).isFalse();
        assertThat(feature.getRemainTimeMachineDays().getValue()).isEqualTo(360);
    }

    @Test
    void testAppsumoTier1Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.APPSUMO_TIER1);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(50);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(20);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(20000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(5L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testAppsumoTier2Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.APPSUMO_TIER2);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(100);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(100);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(50000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(10L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(2000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testAppsumoTier3Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.APPSUMO_TIER3);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(200);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(20L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(3500);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(50000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testAppsumoTier4Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.APPSUMO_TIER4);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(1000);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(500);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(60L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(7000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testAppsumoTier5Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.APPSUMO_TIER5);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(10000);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1000);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(2000000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(100L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(12000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testExclusiveLimitTier1Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.EXCLUSIVE_LIMITED_TIER1);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(50);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(20);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(20000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(5L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(1000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testExclusiveLimitTier2Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.EXCLUSIVE_LIMITED_TIER2);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(100);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(100);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(50000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(10L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(2000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testExclusiveLimitTier3Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.EXCLUSIVE_LIMITED_TIER3);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(200);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(20L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(3500);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(50000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }


    @Test
    void testExclusiveLimitTier4Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.EXCLUSIVE_LIMITED_TIER4);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(1000);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(500);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(60L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(7000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }

    @Test
    void testExclusiveLimitTier5Feature() {
        PlanFeature feature = PlanFeatureLoader.getConfig().get(ProductEnum.EXCLUSIVE_LIMITED_TIER5);
        assertThat(feature).isNotNull();
        assertThat(feature.getSeat().getValue()).isEqualTo(-1);
        assertThat(feature.getFileNodeNums().getValue()).isEqualTo(10000);
        assertThat(feature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1000);
        assertThat(feature.getApiCallNumsPerMonth().getValue()).isEqualTo(2000000);
        assertThat(feature.getAiAgentNums().getValue()).isEqualTo(100L);
        assertThat(feature.getMessageCreditNumsPerMonth().getValue()).isEqualTo(12000);
        assertThat(feature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(feature.getDataExport().getValue()).isEqualTo(true);
        assertThat(feature.getDataCopy().getValue()).isEqualTo(true);
        assertThat(feature.getNodeShare().getValue()).isEqualTo(true);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }
}
