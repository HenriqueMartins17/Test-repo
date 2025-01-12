package com.apitable.enterprise.aliyun.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet;

/**
 * aliyun subscription feature.
 *
 * @author Shawn Deng
 */
public class AliyunSubscriptionFeature implements SubscriptionFeature {

    @Override
    public SubscriptionFeatures.ConsumeFeatures.Seat getSeat() {
        return new SubscriptionFeatures.ConsumeFeatures.Seat(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.CapacitySize getCapacitySize() {
        return new SubscriptionFeatures.ConsumeFeatures.CapacitySize(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FileNodeNums getFileNodeNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FileNodeNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.RowsPerSheet getRowsPerSheet() {
        return new SubscriptionFeatures.ConsumeFeatures.RowsPerSheet(50000L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet getArchivedRowsPerSheet() {
        return new ArchivedRowsPerSheet(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.TotalRows getTotalRows() {
        return new SubscriptionFeatures.ConsumeFeatures.TotalRows(500000000L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.MirrorNums getMirrorNums() {
        return new SubscriptionFeatures.ConsumeFeatures.MirrorNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.AdminNums getAdminNums() {
        return new SubscriptionFeatures.ConsumeFeatures.AdminNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth getApiCallNumsPerMonth() {
        return new SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FormNums getFormNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FormNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.GanttViewNums getGanttViewNums() {
        return new SubscriptionFeatures.ConsumeFeatures.GanttViewNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.CalendarViewNums getCalendarViewNums() {
        return new SubscriptionFeatures.ConsumeFeatures.CalendarViewNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums getFieldPermissionNums() {
        return new SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums(-1L);
    }

    @Override
    public SubscriptionFeatures.ConsumeFeatures.NodePermissionNums getNodePermissionNums() {
        return new SubscriptionFeatures.ConsumeFeatures.NodePermissionNums(-1L);
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
        return new SubscriptionFeatures.SubscribeFeatures.RainbowLabel(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.Watermark getWatermark() {
        return new SubscriptionFeatures.SubscribeFeatures.Watermark(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowInvitation getAllowInvitation() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowInvitation(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin getAllowApplyJoin() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowShare getAllowShare() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowShare(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowExport getAllowExport() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowExport(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowDownload getAllowDownload() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowDownload(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowCopyData getAllowCopyData() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowCopyData(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowEmbed getAllowEmbed() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowEmbed(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo getControlFormBrandLogo() {
        return new SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber getShowMobileNumber() {
        return new SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ContactIsolation getContactIsolation() {
        return new SubscriptionFeatures.SubscribeFeatures.ContactIsolation(true);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog getForbidCreateOnCatalog() {
        return new SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog(true);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainTrashDays getRemainTrashDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainTrashDays(730L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays getRemainTimeMachineDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays(730L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays getRemainRecordActivityDays() {
        return new SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays(730L);
    }

    @Override
    public SubscriptionFeatures.SolidFeatures.AuditQueryDays getAuditQueryDays() {
        return new SubscriptionFeatures.SolidFeatures.AuditQueryDays(730L);
    }

    @Override
    public SubscriptionFeatures.SubscribeFeatures.AllowOrgApi getAllowOrgApi() {
        return new SubscriptionFeatures.SubscribeFeatures.AllowOrgApi(true);
    }
}
