package com.apitable.enterprise.apitablebilling.util;


import cn.hutool.core.util.BooleanUtil;
import com.apitable.enterprise.apitablebilling.enums.BillingFunctionEnum;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.AdminNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ApiQpsNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.AutomationRunNumsPerMonth;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CalendarViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CapacitySize;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FileNodeNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FormNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.GalleryViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.GanttViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.KanbanViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.MessageCreditNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.MirrorNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.NodePermissionNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.RowsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.Seat;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.TotalRows;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.WidgetNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.AuditQueryDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainTrashDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowCopyData;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowDownload;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowEmbed;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowExport;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowInvitation;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowOrgApi;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowShare;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ContactIsolation;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.RainbowLabel;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.SocialConnect;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.Watermark;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Billing Util class.
 */
public class BillingUtil {

    private static final Map<String, Map<BillingFunctionEnum, Long>> productFeature =
        new HashMap<>();

    static {
        Map<BillingFunctionEnum, Long> freeFeature = new HashMap<>();
        freeFeature.put(BillingFunctionEnum.NODES, 30L);
        freeFeature.put(BillingFunctionEnum.ROWS_PER_SHEET, 5000L);
        freeFeature.put(BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET, 1000L);
        freeFeature.put(BillingFunctionEnum.ADMIN_NUM, 3L);
        freeFeature.put(BillingFunctionEnum.TRASH, 14L);
        freeFeature.put(BillingFunctionEnum.TOTAL_SHEET_ROWS, 20000L);
        freeFeature.put(BillingFunctionEnum.FORM_VIEW, 20L);
        freeFeature.put(BillingFunctionEnum.GANTT_VIEW, 10L);
        freeFeature.put(BillingFunctionEnum.FIELD_PERMISSION, 10L);
        freeFeature.put(BillingFunctionEnum.NODE_PERMISSIONS, 10L);
        freeFeature.put(BillingFunctionEnum.CALENDAR_VIEW, 5L);
        freeFeature.put(BillingFunctionEnum.TIME_MACHINE, 14L);
        freeFeature.put(BillingFunctionEnum.RECORD_ACTIVITY, 14L);
        freeFeature.put(BillingFunctionEnum.MIRRORS, 5L);
        freeFeature.put(BillingFunctionEnum.API_CALL, 10000L);
        freeFeature.put(BillingFunctionEnum.API_QPS, 2L);
        freeFeature.put(BillingFunctionEnum.MESSAGE_AUTOMATION_RUN, 100L);
        freeFeature.put(BillingFunctionEnum.MESSAGE_WIDGET, 30L);
        productFeature.put(ProductEnum.FREE.getName(), freeFeature);

        Map<BillingFunctionEnum, Long> plusFeature = new HashMap<>();
        plusFeature.put(BillingFunctionEnum.NODES, 300L);
        plusFeature.put(BillingFunctionEnum.ROWS_PER_SHEET, 10000L);
        plusFeature.put(BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET, 5000L);
        plusFeature.put(BillingFunctionEnum.ADMIN_NUM, 5L);
        plusFeature.put(BillingFunctionEnum.TOTAL_SHEET_ROWS, 3000000L);
        plusFeature.put(BillingFunctionEnum.FORM_VIEW, 100L);
        plusFeature.put(BillingFunctionEnum.GANTT_VIEW, 50L);
        plusFeature.put(BillingFunctionEnum.CALENDAR_VIEW, 50L);
        plusFeature.put(BillingFunctionEnum.FIELD_PERMISSION, 50L);
        plusFeature.put(BillingFunctionEnum.NODE_PERMISSIONS, 50L);
        plusFeature.put(BillingFunctionEnum.TIME_MACHINE, 90L);
        plusFeature.put(BillingFunctionEnum.RECORD_ACTIVITY, 90L);
        plusFeature.put(BillingFunctionEnum.TRASH, 90L);
        plusFeature.put(BillingFunctionEnum.MIRRORS, 50L);
        plusFeature.put(BillingFunctionEnum.API_CALL, 100000L);
        plusFeature.put(BillingFunctionEnum.RAINBOW_LABEL, 1L);
        plusFeature.put(BillingFunctionEnum.API_QPS, 5L);
        plusFeature.put(BillingFunctionEnum.MESSAGE_AUTOMATION_RUN, 10000L);
        plusFeature.put(BillingFunctionEnum.MESSAGE_WIDGET, -1L);
        plusFeature.put(BillingFunctionEnum.AI_AGENT_NUMS, 3L);

        productFeature.put(ProductEnum.PLUS.getName(), plusFeature);

        Map<BillingFunctionEnum, Long> proFeature = new HashMap<>();
        proFeature.put(BillingFunctionEnum.NODES, 1000L);
        proFeature.put(BillingFunctionEnum.ROWS_PER_SHEET, 20000L);
        proFeature.put(BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET, 10000L);
        proFeature.put(BillingFunctionEnum.ADMIN_NUM, 10L);
        proFeature.put(BillingFunctionEnum.TOTAL_SHEET_ROWS, 20000000L);
        proFeature.put(BillingFunctionEnum.FORM_VIEW, 300L);
        proFeature.put(BillingFunctionEnum.GANTT_VIEW, 200L);
        proFeature.put(BillingFunctionEnum.CALENDAR_VIEW, 200L);
        proFeature.put(BillingFunctionEnum.FIELD_PERMISSION, 200L);
        proFeature.put(BillingFunctionEnum.NODE_PERMISSIONS, 200L);
        proFeature.put(BillingFunctionEnum.TIME_MACHINE, 180L);
        proFeature.put(BillingFunctionEnum.RECORD_ACTIVITY, 180L);
        proFeature.put(BillingFunctionEnum.TRASH, 180L);
        proFeature.put(BillingFunctionEnum.MIRRORS, 100L);
        proFeature.put(BillingFunctionEnum.API_CALL, 500000L);
        proFeature.put(BillingFunctionEnum.SECURITY_SETTING_EXPORT, 1L);
        proFeature.put(BillingFunctionEnum.SECURITY_SETTING_MOBILE, 1L);
        proFeature.put(BillingFunctionEnum.RAINBOW_LABEL, 1L);
        proFeature.put(BillingFunctionEnum.EMBED, 1L);
        proFeature.put(BillingFunctionEnum.API_QPS, 10L);
        proFeature.put(BillingFunctionEnum.MESSAGE_AUTOMATION_RUN, 100000L);
        proFeature.put(BillingFunctionEnum.MESSAGE_WIDGET, -1L);
        proFeature.put(BillingFunctionEnum.AI_AGENT_NUMS, 30L);
        proFeature.put(BillingFunctionEnum.CONTROL_FORM_BRAND_LOGO, 1L);

        productFeature.put(ProductEnum.PRO.getName(), proFeature);

        Map<BillingFunctionEnum, Long> enterpriseFeature = new HashMap<>();
        enterpriseFeature.put(BillingFunctionEnum.NODES, 10000L);
        enterpriseFeature.put(BillingFunctionEnum.ROWS_PER_SHEET, 50000L);
        enterpriseFeature.put(BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET, 50000L);
        enterpriseFeature.put(BillingFunctionEnum.ADMIN_NUM, -1L);
        enterpriseFeature.put(BillingFunctionEnum.TOTAL_SHEET_ROWS, 500000000L);
        enterpriseFeature.put(BillingFunctionEnum.FORM_VIEW, -1L);
        enterpriseFeature.put(BillingFunctionEnum.GANTT_VIEW, -1L);
        enterpriseFeature.put(BillingFunctionEnum.GALLERY_VIEW, -1L);
        enterpriseFeature.put(BillingFunctionEnum.KANBAN_VIEW, -1L);
        enterpriseFeature.put(BillingFunctionEnum.CALENDAR_VIEW, -1L);
        enterpriseFeature.put(BillingFunctionEnum.FIELD_PERMISSION, -1L);
        enterpriseFeature.put(BillingFunctionEnum.NODE_PERMISSIONS, -1L);
        enterpriseFeature.put(BillingFunctionEnum.TIME_MACHINE, 730L);
        enterpriseFeature.put(BillingFunctionEnum.RECORD_ACTIVITY, 730L);
        enterpriseFeature.put(BillingFunctionEnum.TRASH, 730L);
        enterpriseFeature.put(BillingFunctionEnum.AUDIT_QUERY, 730L);
        enterpriseFeature.put(BillingFunctionEnum.MIRRORS, -1L);
        enterpriseFeature.put(BillingFunctionEnum.API_CALL, 1000000L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_EXPORT, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_MOBILE, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_INVITE_MEMBER, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_APPLY_JOIN_SPACE, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_SHARE, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_DOWNLOAD_FILE, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_COPY_CELL_DATA, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_ADDRESS_LIST_ISOLATION, 1L);
        enterpriseFeature.put(BillingFunctionEnum.SECURITY_SETTING_CATALOG_MANAGEMENT, 1L);
        enterpriseFeature.put(BillingFunctionEnum.RAINBOW_LABEL, 1L);
        enterpriseFeature.put(BillingFunctionEnum.WATERMARK, 1L);
        enterpriseFeature.put(BillingFunctionEnum.EMBED, 1L);
        enterpriseFeature.put(BillingFunctionEnum.API_QPS, 20L);
        enterpriseFeature.put(BillingFunctionEnum.ORG_API, 1L);
        enterpriseFeature.put(BillingFunctionEnum.MESSAGE_AUTOMATION_RUN, 500000L);
        enterpriseFeature.put(BillingFunctionEnum.MESSAGE_WIDGET, -1L);
        enterpriseFeature.put(BillingFunctionEnum.AI_AGENT_NUMS, 100L);
        enterpriseFeature.put(BillingFunctionEnum.CONTROL_FORM_BRAND_LOGO, 1L);

        productFeature.put(ProductEnum.ENTERPRISE.getName(), enterpriseFeature);

        Map<BillingFunctionEnum, Long> privateCloudFeature = new HashMap<>();
        privateCloudFeature.put(BillingFunctionEnum.NODES, -1L);
        privateCloudFeature.put(BillingFunctionEnum.ROWS_PER_SHEET, 50000L);
        privateCloudFeature.put(BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET, -1L);
        privateCloudFeature.put(BillingFunctionEnum.ADMIN_NUM, -1L);
        privateCloudFeature.put(BillingFunctionEnum.TOTAL_SHEET_ROWS, 500000000L);
        privateCloudFeature.put(BillingFunctionEnum.FORM_VIEW, -1L);
        privateCloudFeature.put(BillingFunctionEnum.GANTT_VIEW, -1L);
        privateCloudFeature.put(BillingFunctionEnum.CALENDAR_VIEW, -1L);
        privateCloudFeature.put(BillingFunctionEnum.GALLERY_VIEW, -1L);
        privateCloudFeature.put(BillingFunctionEnum.KANBAN_VIEW, -1L);
        privateCloudFeature.put(BillingFunctionEnum.FIELD_PERMISSION, -1L);
        privateCloudFeature.put(BillingFunctionEnum.NODE_PERMISSIONS, -1L);
        privateCloudFeature.put(BillingFunctionEnum.TIME_MACHINE, 730L);
        privateCloudFeature.put(BillingFunctionEnum.RECORD_ACTIVITY, 730L);
        privateCloudFeature.put(BillingFunctionEnum.TRASH, 730L);
        privateCloudFeature.put(BillingFunctionEnum.MIRRORS, -1L);
        privateCloudFeature.put(BillingFunctionEnum.AUDIT_QUERY, 730L);
        privateCloudFeature.put(BillingFunctionEnum.API_CALL, -1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_SHARE, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_EXPORT, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_MOBILE, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_INVITE_MEMBER, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_APPLY_JOIN_SPACE, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_COPY_CELL_DATA, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_DOWNLOAD_FILE, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_ADDRESS_LIST_ISOLATION, 1L);
        privateCloudFeature.put(BillingFunctionEnum.SECURITY_SETTING_CATALOG_MANAGEMENT, 1L);
        privateCloudFeature.put(BillingFunctionEnum.RAINBOW_LABEL, 1L);
        privateCloudFeature.put(BillingFunctionEnum.WATERMARK, 1L);
        privateCloudFeature.put(BillingFunctionEnum.EMBED, 1L);
        privateCloudFeature.put(BillingFunctionEnum.API_QPS, -1L);
        privateCloudFeature.put(BillingFunctionEnum.ORG_API, 1L);
        privateCloudFeature.put(BillingFunctionEnum.MESSAGE_AUTOMATION_RUN, -1L);
        privateCloudFeature.put(BillingFunctionEnum.MESSAGE_WIDGET, -1L);
        privateCloudFeature.put(BillingFunctionEnum.CONTROL_FORM_BRAND_LOGO, 1L);

        productFeature.put(ProductEnum.PRIVATE_CLOUD.getName(), privateCloudFeature);
    }

    /**
     * build subscription feature object.
     *
     * @param product product enum
     * @param seats   product seats number
     * @param isTrial is trial
     * @return SubscriptionFeature
     */
    public static SubscriptionFeature buildFeature(ProductEnum product, int seats,
                                                   boolean isTrial) {
        BillingSubscriptionFeature billingPlanFeature = new BillingSubscriptionFeature();
        Map<BillingFunctionEnum, Long> featureMap = productFeature.get(product.getName());
        if (ProductEnum.isAppsumoProduct(product.getName())) {
            billingPlanFeature.setSeat(
                new Seat(buildConsumeFunctionValue(featureMap, BillingFunctionEnum.SEATS))
            );
            billingPlanFeature.setCapacitySize(new CapacitySize(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.CAPACITY)));
            billingPlanFeature.setMessageCreditNums(new MessageCreditNums(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.MESSAGE_CREDIT)));
        } else {
            billingPlanFeature.setSeat(new Seat((long) seats, true));
            billingPlanFeature.setCapacitySize(
                new CapacitySize(buildUsageFunctionValue(product, seats)));
            billingPlanFeature.setMessageCreditNums(
                new MessageCreditNums(buildCredit(product, seats, isTrial)));
        }
        billingPlanFeature.setAiAgentNums(
            new SubscriptionFeatures.ConsumeFeatures.AiAgentNums(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.AI_AGENT_NUMS)
            )
        );
        billingPlanFeature.setFileNodeNums(
            new FileNodeNums(buildConsumeFunctionValue(featureMap, BillingFunctionEnum.NODES)));
        billingPlanFeature.setAdminNums(
            new AdminNums(buildConsumeFunctionValue(featureMap, BillingFunctionEnum.ADMIN_NUM)));
        billingPlanFeature.setRowsPerSheet(new RowsPerSheet(
            buildConsumeFunctionValue(featureMap, BillingFunctionEnum.ROWS_PER_SHEET)));
        billingPlanFeature.setArchivedRowsPerSheet(new ArchivedRowsPerSheet(
            buildConsumeFunctionValue(featureMap, BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET)));
        billingPlanFeature.setTotalRows(new TotalRows(
            buildConsumeFunctionValue(featureMap, BillingFunctionEnum.TOTAL_SHEET_ROWS)));
        billingPlanFeature.setRemainTrashDays(
            new RemainTrashDays(buildSolidFunctionValue(featureMap, BillingFunctionEnum.TRASH)));
        billingPlanFeature.setGalleryViewNums(new GalleryViewNums(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.GALLERY_VIEW)));
        billingPlanFeature.setKanbanViewNums(
            new KanbanViewNums(
                buildSolidFunctionValue(featureMap, BillingFunctionEnum.KANBAN_VIEW)));
        billingPlanFeature.setFormNums(
            new FormNums(buildSolidFunctionValue(featureMap, BillingFunctionEnum.FORM_VIEW)));
        billingPlanFeature.setGanttViewNums(
            new GanttViewNums(buildSolidFunctionValue(featureMap, BillingFunctionEnum.GANTT_VIEW)));
        billingPlanFeature.setCalendarViewNums(new CalendarViewNums(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.CALENDAR_VIEW)));
        billingPlanFeature.setFieldPermissionNums(new FieldPermissionNums(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.FIELD_PERMISSION)));
        billingPlanFeature.setNodePermissionNums(new NodePermissionNums(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.NODE_PERMISSIONS)));
        billingPlanFeature.setRemainTimeMachineDays(new RemainTimeMachineDays(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.TIME_MACHINE)));
        billingPlanFeature.setRainbowLabel(new RainbowLabel(defaultIfNull(
            buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.RAINBOW_LABEL))));
        billingPlanFeature.setSocialConnect(new SocialConnect(defaultIfNull(
            buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.SOCIAL_CONNECT))));
        billingPlanFeature.setWatermark(new Watermark(
            defaultIfNull(buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.WATERMARK))));
        billingPlanFeature.setRemainRecordActivityDays(new RemainRecordActivityDays(
            buildSolidFunctionValue(featureMap, BillingFunctionEnum.RECORD_ACTIVITY)));
        billingPlanFeature.setAllowInvitation(new AllowInvitation(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_INVITE_MEMBER))));
        billingPlanFeature.setAllowApplyJoin(new AllowApplyJoin(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_APPLY_JOIN_SPACE))));
        billingPlanFeature.setAllowCopyData(new AllowCopyData(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_COPY_CELL_DATA))));
        billingPlanFeature.setAllowDownload(new AllowDownload(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_DOWNLOAD_FILE))));
        billingPlanFeature.setAllowExport(new AllowExport(defaultIfNull(
            buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.SECURITY_SETTING_EXPORT))));
        billingPlanFeature.setAllowShare(new AllowShare(defaultIfNull(
            buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.SECURITY_SETTING_SHARE))));
        billingPlanFeature.setAllowEmbed(new AllowEmbed(
            defaultIfNull(buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.EMBED))));
        billingPlanFeature.setControlFormBrandLogo(new ControlFormBrandLogo(
            defaultIfNull(buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.CONTROL_FORM_BRAND_LOGO))));
        billingPlanFeature.setShowMobileNumber(new ShowMobileNumber(defaultIfNull(
            buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.SECURITY_SETTING_MOBILE))));
        Long auditQueryDays = buildSolidFunctionValue(featureMap, BillingFunctionEnum.AUDIT_QUERY);
        billingPlanFeature.setAuditQueryDays(new AuditQueryDays(auditQueryDays));
        billingPlanFeature.setAuditQuery(
            new SubscriptionFeatures.SubscribeFeatures.AuditQuery(
                auditQueryDays != null && auditQueryDays > 0)
        );
        billingPlanFeature.setContactIsolation(new ContactIsolation(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_ADDRESS_LIST_ISOLATION))));
        billingPlanFeature.setForbidCreateOnCatalog(new ForbidCreateOnCatalog(defaultIfNull(
            buildSubscribeFunctionValue(featureMap,
                BillingFunctionEnum.SECURITY_SETTING_CATALOG_MANAGEMENT))));
        billingPlanFeature.setMirrorNums(
            new MirrorNums(buildSolidFunctionValue(featureMap, BillingFunctionEnum.MIRRORS)));
        billingPlanFeature.setApiCallNumsPerMonth(
            new ApiCallNumsPerMonth(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.API_CALL)));
        billingPlanFeature.setApiQpsNums(
            new ApiQpsNums(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.API_QPS)));
        billingPlanFeature.setAllowOrgApi(
            new AllowOrgApi(buildSubscribeFunctionValue(featureMap, BillingFunctionEnum.ORG_API)));
        billingPlanFeature.setAutomationRunNumsPerMonth(
            new AutomationRunNumsPerMonth(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.MESSAGE_AUTOMATION_RUN)));
        billingPlanFeature.setWidgetNums(
            new WidgetNums(
                buildConsumeFunctionValue(featureMap, BillingFunctionEnum.MESSAGE_WIDGET)));
        return billingPlanFeature;
    }

    private static Long buildConsumeFunctionValue(Map<BillingFunctionEnum, Long> featureMap,
                                                  BillingFunctionEnum function) {
        return featureMap.getOrDefault(function, 0L);
    }

    private static Boolean buildSubscribeFunctionValue(Map<BillingFunctionEnum, Long> featureMap,
                                                       BillingFunctionEnum function) {
        return BooleanUtil.toBoolean(featureMap.getOrDefault(function, 0L).toString());
    }

    private static Long buildSolidFunctionValue(Map<BillingFunctionEnum, Long> featureMap,
                                                BillingFunctionEnum function) {
        return featureMap.getOrDefault(function, 0L);
    }

    /**
     * build usage function type value.
     *
     * @param productEnum product enum
     * @param perUnit     per unit
     * @return value
     */
    private static Long buildCredit(ProductEnum productEnum, int perUnit, boolean isTrial) {
        long value = 0L;
        switch (productEnum) {
            case FREE: {
                value = 5 * 50L;
                break;
            }
            case PRIVATE_CLOUD:
                value = -1L;
                break;
            case PLUS: {
                value = isTrial ? 250L : perUnit * 500L;
                break;
            }
            case PRO: {
                value = isTrial ? 250L : perUnit * 1200L;
                break;
            }
            case ENTERPRISE: {
                value = isTrial ? 250L : perUnit * 3000L;
                break;
            }
            default:
                break;
        }
        return value;
    }

    /**
     * build usage function type value.
     *
     * @param productEnum product enum
     * @param perUnit     per unit
     * @return value
     */
    private static Long buildUsageFunctionValue(ProductEnum productEnum, int perUnit) {
        long value = 0L;
        switch (productEnum) {
            case FREE: {
                value = 1024 * 1024 * 1024L;
                break;
            }
            case PLUS: {
                value = perUnit * 5 * 1024 * 1024 * 1024L;
                break;
            }
            case PRO: {
                value = perUnit * 7 * 1024 * 1024 * 1024L;
                break;
            }
            case ENTERPRISE: {
                value = perUnit * 10 * 1024 * 1024 * 1024L;
                break;
            }
            case PRIVATE_CLOUD:
                value = -1L;
                break;
            default:
                break;
        }
        return value;
    }

    private static Boolean defaultIfNull(Boolean bool) {
        if (bool == null) {
            return Boolean.FALSE;
        }
        return bool;
    }

    /**
     * unix timestamp to LocalDateTime.
     *
     * @param timestamp unix timestamp
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
    }
}
