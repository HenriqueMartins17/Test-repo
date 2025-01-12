/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.util;

import cn.hutool.core.collection.CollUtil;
import com.apitable.enterprise.vikabilling.enums.BillingFunctionEnum;
import com.apitable.enterprise.vikabilling.enums.BillingFunctionType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.interfaces.model.BillingSubscriptionFeature;
import com.apitable.enterprise.vikabilling.setting.BillingConfig;
import com.apitable.enterprise.vikabilling.setting.BillingConfigLoader;
import com.apitable.enterprise.vikabilling.setting.Event;
import com.apitable.enterprise.vikabilling.setting.Feature;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.PriceList;
import com.apitable.enterprise.vikabilling.setting.Product;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.unit.DataSize;

/**
 * <p>
 * Billing Config Manager.
 * </p>
 *
 * @author Shawn Deng
 */
@Slf4j
public class BillingConfigManager {

    private static final BillingConfig BILLING_CONFIG = BillingConfigLoader.getConfig();

    private BillingConfigManager() {
    }


    public static BillingConfig getBillingConfig() {
        return BILLING_CONFIG;
    }

    /**
     * Get product by name.
     *
     * @param productName product name
     * @return Product
     */
    public static Product getProductByName(String productName) {
        return BILLING_CONFIG.getProducts().get(productName);
    }

    /**
     * Get free products.
     *
     * @param channel product channel
     * @return Product
     */
    public static Product getCurrentFreeProduct(ProductChannel channel) {
        return BILLING_CONFIG.getProducts().entrySet().stream()
            .filter((entry) -> ProductChannel.of(entry.getValue().getChannel()) == channel
                && entry.getValue().isFree())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Billing Error"))
            .getValue();
    }

    /**
     * get free plan name.
     *
     * @return free name
     */
    private static String getFreePlanName(ProductChannel channel) {
        Product freeProduct = getCurrentFreeProduct(channel);
        String basePlanName = freeProduct.getPlans().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Billing Plan Error"));
        Assert.notNull(basePlanName, "Billing Plan Error");
        return basePlanName;
    }

    /**
     * get free plan on special channel.
     *
     * @return Plan channel
     */
    public static Plan getFreePlan(ProductChannel channel) {
        String freePlanName = getFreePlanName(channel);
        return BILLING_CONFIG.getPlans().get(freePlanName);
    }

    /**
     * get free plan on special channel.
     *
     * @param productName product name
     * @param seat        seat
     * @return Plan
     */
    public static Plan getPlan(String productName, int seat) {
        // product name is upper case
        ProductEnum product = ProductEnum.valueOf(productName.toUpperCase(Locale.ROOT));
        return getPlan(product, seat);
    }

    /**
     * get free plan on special channel.
     *
     * @param product product
     * @param seat    seat
     * @return Plan
     */
    public static Plan getPlan(ProductEnum product, int seat) {
        return BILLING_CONFIG.getPlans().values().stream()
            .filter(plan -> plan.getProduct().equals(product.getName()) && plan.getSeats() == seat)
            .findFirst()
            .orElse(null);
    }

    /**
     * get price list on product.
     *
     * @param product product
     * @return price list
     */
    public static List<Price> getPriceList(ProductEnum product) {
        List<String> priceIds = BILLING_CONFIG.getProducts().get(product.getName()).getPrices();
        List<Price> prices = new ArrayList<>();
        priceIds.forEach(priceId -> prices.add(BILLING_CONFIG.getPrices().get(priceId)));
        return prices.stream().filter(price -> price.getSeat() != 0 && price.isOnline())
            .sorted(Comparator.comparingInt(Price::getSeat).thenComparingInt(Price::getMonth))
            .collect(Collectors.toList());
    }

    public static Price getPriceBySeatAndMonth(String productName, int seat, int month) {
        ProductEnum product = ProductEnum.valueOf(productName);
        return getPriceBySeatAndMonths(product, seat, month);
    }

    /**
     * get price by seat and month.
     *
     * @param product product
     * @param seat    seat
     * @param month   month
     * @return price
     */
    public static Price getPriceBySeatAndMonths(ProductEnum product, int seat, int month) {
        if (seat == 0) {
            return null;
        }
        return BILLING_CONFIG.getPrices().values().stream()
            .filter(price -> price.getProduct().equals(product.getName())
                && price.getSeat() == seat && price.getMonth() == month
                && price.isOnline())
            .findFirst()
            .orElse(null);
    }

    /**
     * get price by seat and month.
     *
     * @param now now
     * @return price
     */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public static Event getEventOnEffectiveDate(LocalDate now) {
        return getBillingConfig().getEvents().values().stream()
            .filter(event -> event.getStartDate().compareTo(now) <= 0
                && event.getEndDate().compareTo(now) >= 0)
            .findFirst().orElse(null);
    }

    public static Event getByEventId(String eventId) {
        return BILLING_CONFIG.getEvents().get(eventId);
    }

    public static PriceList getByPriceListId(String priceListId) {
        return BILLING_CONFIG.getPricelist().get(priceListId);
    }

    /**
     * build plan feature.
     *
     * @param basePlan   base plan
     * @param addOnPlans list of add-on plan
     * @return BillingPlanFeature
     */
    public static SubscriptionFeature buildPlanFeature(Plan basePlan, List<Plan> addOnPlans) {
        BillingSubscriptionFeature billingPlanFeature = new BillingSubscriptionFeature();
        // Get the feature points that the basic subscription plan has
        Map<String, Feature> basePlanFeatureMap = BILLING_CONFIG.getFeatures().entrySet().stream()
            .filter(entry -> basePlan.getFeatures().contains(entry.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        // The add-on plan does not have the following attributes
        billingPlanFeature.setSeat(new Seat(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.SEATS, Long.class)));
        billingPlanFeature.setFileNodeNums(new FileNodeNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.NODES, Long.class)));
        billingPlanFeature.setAdminNums(new AdminNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.ADMIN_NUM, Long.class)));
        billingPlanFeature.setRowsPerSheet(new RowsPerSheet(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.ROWS_PER_SHEET,
                Long.class)));
        billingPlanFeature.setArchivedRowsPerSheet(new ArchivedRowsPerSheet(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.ARCHIVED_ROWS_PER_SHEET,
                Long.class)));
        billingPlanFeature.setTotalRows(new TotalRows(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.TOTAL_SHEET_ROWS,
                Long.class)));
        billingPlanFeature.setRemainTrashDays(new RemainTrashDays(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.TRASH, Long.class)));
        billingPlanFeature.setGalleryViewNums(new GalleryViewNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.GALLERY_VIEW, Long.class)));
        billingPlanFeature.setKanbanViewNums(new KanbanViewNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.KANBAN_VIEW, Long.class)));
        billingPlanFeature.setFormNums(new FormNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.FORM_VIEW, Long.class)));
        billingPlanFeature.setGanttViewNums(new GanttViewNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.GANTT_VIEW, Long.class)));
        billingPlanFeature.setCalendarViewNums(new CalendarViewNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.CALENDAR_VIEW,
                Long.class)));
        billingPlanFeature.setFieldPermissionNums(new FieldPermissionNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.FIELD_PERMISSION,
                Long.class)));
        billingPlanFeature.setNodePermissionNums(new NodePermissionNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.NODE_PERMISSIONS,
                Long.class)));
        billingPlanFeature.setRemainTimeMachineDays(new RemainTimeMachineDays(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.TIME_MACHINE, Long.class)));
        billingPlanFeature.setRainbowLabel(new RainbowLabel(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.RAINBOW_LABEL,
                Boolean.class))));
        billingPlanFeature.setWatermark(new Watermark(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.WATERMARK,
                Boolean.class))));
        billingPlanFeature.setSocialConnect(new SocialConnect(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.INTEGRATION_FEISHU,
                Boolean.class))));
        billingPlanFeature.setRemainRecordActivityDays(new RemainRecordActivityDays(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.RECORD_ACTIVITY,
                Long.class)));
        billingPlanFeature.setAllowInvitation(new AllowInvitation(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_INVITE_MEMBER, Boolean.class))));
        billingPlanFeature.setAllowApplyJoin(new AllowApplyJoin(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_APPLY_JOIN_SPACE, Boolean.class))));
        billingPlanFeature.setAllowCopyData(new AllowCopyData(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_COPY_CELL_DATA, Boolean.class))));
        billingPlanFeature.setAllowDownload(new AllowDownload(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_DOWNLOAD_FILE, Boolean.class))));
        billingPlanFeature.setAllowExport(new AllowExport(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.SECURITY_SETTING_EXPORT,
                Boolean.class))));
        billingPlanFeature.setAllowShare(new AllowShare(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.SECURITY_SETTING_SHARE,
                Boolean.class))));
        billingPlanFeature.setAllowEmbed(new AllowEmbed(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.EMBED, Boolean.class))));
        billingPlanFeature.setControlFormBrandLogo(new ControlFormBrandLogo(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.CONTROL_FORM_BRAND_LOGO,
                Boolean.class))));
        billingPlanFeature.setShowMobileNumber(new ShowMobileNumber(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.SECURITY_SETTING_MOBILE,
                Boolean.class))));
        Long auditQueryDays =
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.AUDIT_QUERY, Long.class);
        billingPlanFeature.setAuditQueryDays(new AuditQueryDays(auditQueryDays));
        billingPlanFeature.setAuditQuery(
            new SubscriptionFeatures.SubscribeFeatures.AuditQuery(
                auditQueryDays != null && auditQueryDays > 0));
        billingPlanFeature.setContactIsolation(new ContactIsolation(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_ADDRESS_LIST_ISOLATION, Boolean.class))));
        billingPlanFeature.setForbidCreateOnCatalog(new ForbidCreateOnCatalog(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap,
                BillingFunctionEnum.SECURITY_SETTING_CATALOG_MANAGEMENT, Boolean.class))));
        billingPlanFeature.setMirrorNums(new MirrorNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.MIRRORS, Long.class)));
        billingPlanFeature.setAllowOrgApi(new AllowOrgApi(defaultIfNull(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.ORG_API, Boolean.class))));
        billingPlanFeature.setApiQpsNums(new ApiQpsNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.API_QPS, Long.class)));
        Long messageCreditNums = buildMessageCreditValue(basePlan);
        billingPlanFeature.setAiAgentNums(
            new SubscriptionFeatures.ConsumeFeatures.AiAgentNums(
                buildAiAgentNumsValue(basePlan, messageCreditNums)
            )
        );
        billingPlanFeature.setMessageCreditNums(new MessageCreditNums(messageCreditNums));
        billingPlanFeature.setAutomationRunNumsPerMonth(new AutomationRunNumsPerMonth(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.MESSAGE_AUTOMATION_RUN,
                Long.class)));
        billingPlanFeature.setWidgetNums(new WidgetNums(
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.MESSAGE_WIDGET,
                Long.class)));

        // Stackable subscription plan limits
        Long baseCapacitySize =
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.CAPACITY, Long.class);
        if (baseCapacitySize != null) {
            billingPlanFeature.setCapacitySize(new CapacitySize(
                baseCapacitySize != -1 ? baseCapacitySize * 1024 * 1024 * 1024L :
                    baseCapacitySize));
        }
        Long baseApiCalls =
            getPlanFeatureValue(basePlanFeatureMap, BillingFunctionEnum.API_CALL, Long.class);
        if (baseApiCalls != null) {
            billingPlanFeature.setApiCallNumsPerMonth(new ApiCallNumsPerMonth(baseApiCalls));
        }
        // Option to obtain add-on packages
        if (CollUtil.isNotEmpty(addOnPlans)) {
            // There is an add-on plan, and the value of the add-on plan is used
            addOnPlans.forEach(addOnPlan -> {
                List<Feature> features = BILLING_CONFIG.getFeatures().entrySet()
                    .stream()
                    .filter(entry -> addOnPlan.getFeatures().contains(entry.getKey()))
                    .map(Entry::getValue).toList();
                for (Feature feature : features) {
                    // Added capacity of accessories
                    if (feature.getFunction().equals(BillingFunctionEnum.CAPACITY.getCode())) {
                        long additionalCapacity =
                            billingPlanFeature.getCapacitySize().getValue() != null
                                ? getTrueSpecificationByUnit(feature.getSpecification(),
                                feature.getUnit())
                                : feature.getSpecification();
                        billingPlanFeature.getCapacitySize()
                            .plus(DataSize.ofBytes(additionalCapacity));
                        break;
                    }
                    // API Usage
                    if (feature.getFunction().equals(BillingFunctionEnum.API_CALL.getCode())) {
                        billingPlanFeature.getApiCallNumsPerMonth()
                            .plus(feature.getSpecification());
                        break;
                    }
                }
            });
        }
        return billingPlanFeature;
    }

    private static Long getTrueSpecificationByUnit(Long specification, String unit) {
        if ("g".equalsIgnoreCase(unit)) {
            return specification * 1024 * 1024 * 1024L;
        } else if ("mb".equalsIgnoreCase(unit)) {
            return specification * 1024 * 1024L;
        } else {
            throw new IllegalArgumentException("lost specification unit");
        }
    }

    private static Long buildAiAgentNumsValue(Plan plan, Long messageCreditNums) {
        String productName = plan.getProduct();
        ProductEnum productEnum = ProductEnum.valueOf(productName.toUpperCase(Locale.ROOT));
        return switch (productEnum) {
            case SILVER, FEISHU_STANDARD, WECOM_STANDARD, DINGTALK_STANDARD ->
                messageCreditNums / 100L;
            case GOLD -> messageCreditNums / 200L;
            case ENTERPRISE -> messageCreditNums / 1200L;
            case FEISHU_PROFESSION, WECOM_PROFESSION, DINGTALK_PROFESSION ->
                messageCreditNums / 150L;
            case FEISHU_ENTERPRISE, WECOM_ENTERPRISE, DINGTALK_ENTERPRISE ->
                messageCreditNums / 500L;
            default -> 0L;
        };
    }

    private static Long buildMessageCreditValue(Plan plan) {
        String productName = plan.getProduct();
        ProductEnum productEnum = ProductEnum.valueOf(productName.toUpperCase(Locale.ROOT));
        int seat = plan.getSeats();
        return switch (productEnum) {
            case SILVER, FEISHU_STANDARD, WECOM_STANDARD, DINGTALK_STANDARD -> 100L * seat;
            case GOLD -> 200L * seat;
            case ENTERPRISE -> 1200L * seat;
            case FEISHU_PROFESSION, WECOM_PROFESSION, DINGTALK_PROFESSION -> 150L * seat;
            case FEISHU_ENTERPRISE, WECOM_ENTERPRISE, DINGTALK_ENTERPRISE -> 500L * seat;
            default -> 0L;
        };
    }

    /**
     * get subscription specification value.
     *
     * @param featureMap   feature set
     * @param functionEnum function code
     * @param clzType      feature value data type
     * @param <T>          data type
     * @return specification value
     */
    public static <T> T getPlanFeatureValue(Map<String, Feature> featureMap,
                                            BillingFunctionEnum functionEnum, Class<T> clzType) {
        Feature feature = getPlanFeature(featureMap, functionEnum.getCode());
        if (feature == null) {
            return null;
        }
        BillingFunctionType functionType = BillingFunctionType.of(feature.getFunctionType());
        if (functionType != null) {
            if (functionType.isSubscribe()) {
                return clzType.cast(Boolean.TRUE);
            }
            return clzType.cast(feature.getSpecification());
        }
        return null;
    }

    private static Boolean defaultIfNull(Boolean bool) {
        if (bool == null) {
            return Boolean.FALSE;
        }
        return bool;
    }

    /**
     * Get the specified function point definition.
     *
     * @param planFeatureMap Subscription plan feature set
     * @param functionId     function identification
     * @return Feature
     */
    public static Feature getPlanFeature(Map<String, Feature> planFeatureMap, String functionId) {
        Feature feature = null;
        for (Entry<String, Feature> entry : planFeatureMap.entrySet()) {
            if (entry.getValue().getFunction().contentEquals(functionId)) {
                feature = entry.getValue();
                break;
            }
        }
        return feature;
    }

    /**
     * Get 300MB capacity plan.
     *
     * @return Plan
     */
    public static Plan getGiftPlan() {
        return BILLING_CONFIG.getPlans().values().stream()
            .filter(Plan::isGift)
            .findFirst()
            .orElse(null);
    }

}
