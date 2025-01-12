package com.apitable.enterprise.selfhostbilling.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures;

/**
 * limit self-host subscription feature.
 *
 * @author Shawn Deng
 */
public class LimitSelfHostSubscriptionFeature implements SubscriptionFeature {

    @Override
    public SubscriptionFeatures.ConsumeFeatures.Seat getSeat() {
        return new SubscriptionFeatures.ConsumeFeatures.Seat(5L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.CapacitySize getCapacitySize() {
        return new SubscriptionFeatures.ConsumeFeatures.CapacitySize(1024 * 1024 * 1024L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FileNodeNums getFileNodeNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FileNodeNums(30L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.RowsPerSheet getRowsPerSheet() {
        return new SubscriptionFeatures.ConsumeFeatures.RowsPerSheet(5000L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet getArchivedRowsPerSheet() {
        return new SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.TotalRows getTotalRows() {
        return new SubscriptionFeatures.ConsumeFeatures.TotalRows(20000L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.MirrorNums getMirrorNums() {
        return new SubscriptionFeatures.ConsumeFeatures.MirrorNums(5L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.AdminNums getAdminNums() {
        return new SubscriptionFeatures.ConsumeFeatures.AdminNums(3L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth getApiCallNumsPerMonth() {
        return new SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth(10000L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FormNums getFormNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FormNums(20L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.GanttViewNums getGanttViewNums() {
        return new SubscriptionFeatures.ConsumeFeatures.GanttViewNums(10L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.CalendarViewNums getCalendarViewNums() {
        return new SubscriptionFeatures.ConsumeFeatures.CalendarViewNums(5L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums getFieldPermissionNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums(10L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.NodePermissionNums getNodePermissionNums() {
        return new SubscriptionFeatures.ConsumeFeatures.NodePermissionNums(10L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.ApiQpsNums getApiQpsNums() {
        return new SubscriptionFeatures.ConsumeFeatures.ApiQpsNums(5L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.AutomationRunNumsPerMonth getAutomationRunNumsPerMonth() {
        return new SubscriptionFeatures.ConsumeFeatures.AutomationRunNumsPerMonth(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.WidgetNums getWidgetNums() {
        return new SubscriptionFeatures.ConsumeFeatures.WidgetNums(-1L);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.SocialConnect getSocialConnect() {
        return new SubscriptionFeatures.SubscribeFeatures.SocialConnect(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.RainbowLabel getRainbowLabel() {
        return new SubscriptionFeatures.SubscribeFeatures.RainbowLabel(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.Watermark getWatermark() {
        return new SubscriptionFeatures.SubscribeFeatures.Watermark(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowInvitation getAllowInvitation() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowInvitation(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin getAllowApplyJoin() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowShare getAllowShare() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowShare(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowExport getAllowExport() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowExport(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowDownload getAllowDownload() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowDownload(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowCopyData getAllowCopyData() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowCopyData(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowEmbed getAllowEmbed() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowEmbed(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo getControlFormBrandLogo() {
        return new SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber getShowMobileNumber() {
        return new SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ContactIsolation getContactIsolation() {
        return new SubscriptionFeatures.SubscribeFeatures.ContactIsolation(false);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog getForbidCreateOnCatalog() {
        return new SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog(false);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainTrashDays getRemainTrashDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainTrashDays(14L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays getRemainTimeMachineDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays(14L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays getRemainRecordActivityDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays(14L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.AuditQueryDays getAuditQueryDays() {
        return new SubscriptionFeatures.SolidFeatures.AuditQueryDays(0L);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowOrgApi getAllowOrgApi() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowOrgApi(false);
    }

}
