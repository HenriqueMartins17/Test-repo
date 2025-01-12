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

import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.buildPlanFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.setting.Event;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.PriceList;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


public class BillingConfigManagerTest {

    /**
     * Product Channel - Proprietary Cloud Billing: Flagship Product
     */
    @Test
    public void testGetFreeProductByPrivateChannel() {
        Product privateFreeProduct =
            BillingConfigManager.getCurrentFreeProduct(ProductChannel.PRIVATE);
        assertNotNull(privateFreeProduct);
        assertEquals(privateFreeProduct.getId(), ProductEnum.PRIVATE_CLOUD.getName());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(privateFreeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(-1);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getCapacitySize().isUnlimited()).isTrue();
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(730);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(730);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(500000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(-1);
        assertThat(planFeature.getSocialConnect().getValue()).isTrue();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isTrue();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(-1);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isTrue();
    }

    /**
     * Product Channels - Alibaba Cloud Computing Nest Billing: Free Products
     */
    @Test
    public void testGetFreeProductByAliyunChannel() {
        Product privateFreeProduct =
            BillingConfigManager.getCurrentFreeProduct(ProductChannel.ALIYUN);
        assertNotNull(privateFreeProduct);
        assertEquals(privateFreeProduct.getId(), ProductEnum.ATLAS.getName());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(privateFreeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(-1L);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getCapacitySize().isUnlimited()).isTrue();
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(730);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(730);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(50000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(-1);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(-1);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isTrue();

    }

    /**
     * Product channel - self-operated billing: bronze-level product
     */
    @Test
    public void testGetFreeProductByVikaChannel() {
        Product freeProduct = BillingConfigManager.getCurrentFreeProduct(ProductChannel.VIKA);
        assertNotNull(freeProduct);
        assertEquals(freeProduct.getId(), ProductEnum.BRONZE.getName());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(freeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(5);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(30);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(20);
        assertThat(planFeature.getCapacitySize().getValue().toGigabytes())
            .isEqualTo(1);
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(3);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(1000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isFalse();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(2L);
        assertThat(planFeature.getMessageCreditNums().getValue()).isZero();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(100L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(30L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();

    }

    @Test
    public void testSilver2ProductPlanFeature() {
        Plan silverPlan = BillingConfigManager.getPlan(ProductEnum.SILVER, 2);
        Plan addOnPlan = BillingConfigManager.getBillingConfig().getPlans().get("capacity_300_MB");
        SubscriptionFeature planFeature =
            buildPlanFeature(silverPlan, Collections.singletonList(addOnPlan));
        assertThat(planFeature.getSeat().getValue()).isEqualTo(2);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(planFeature.getMirrorNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(100);
        assertThat(planFeature.getCapacitySize().getValue().toBytes())
            .isEqualTo((10 * 1024 * 1024 * 1024L) + (300 * 1024 * 1024L));
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(90);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(90);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(3000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(10000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(200L);
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(10000L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();
    }

    @Test
    public void testSilver100ProductPlanFeature() {
        Plan silverPlan = BillingConfigManager.getPlan(ProductEnum.SILVER, 100);
        Plan addOnPlan = BillingConfigManager.getBillingConfig().getPlans().get("capacity_300_MB");
        SubscriptionFeature planFeature =
            buildPlanFeature(silverPlan, Collections.singletonList(addOnPlan));
        assertThat(planFeature.getSeat().getValue()).isEqualTo(100);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(300);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(100000);
        assertThat(planFeature.getMirrorNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(100);
        assertThat(planFeature.getCapacitySize().getValue().toBytes())
            .isEqualTo((500 * 1024 * 1024 * 1024L) + (300 * 1024 * 1024L));
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(50);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(90);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(90);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(3000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(10000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(10000L);
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(10000L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();
    }

    @Test
    public void testGoldSeatsProductPlanFeature() {
        Plan silverPlan = BillingConfigManager.getPlan(ProductEnum.GOLD, 200);
        Plan addOnPlan = BillingConfigManager.getBillingConfig().getPlans().get("capacity_300_MB");
        SubscriptionFeature planFeature =
            buildPlanFeature(silverPlan, Collections.singletonList(addOnPlan));
        assertThat(planFeature.getSeat().getValue()).isEqualTo(200);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(1000);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(300);
        assertThat(planFeature.getCapacitySize().getValue().toBytes())
            .isEqualTo((1400 * 1024 * 1024 * 1024L) + (300 * 1024 * 1024L));
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(180);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(180);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(20000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(10000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(40000L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isTrue();

    }

    @Test
    public void testGold30SeatsProductPlanFeature() {
        Plan silverPlan = BillingConfigManager.getPlan(ProductEnum.GOLD, 30);
        Plan addOnPlan = BillingConfigManager.getBillingConfig().getPlans().get("capacity_300_MB");
        SubscriptionFeature planFeature =
            buildPlanFeature(silverPlan, Collections.singletonList(addOnPlan));
        assertThat(planFeature.getSeat().getValue()).isEqualTo(30);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(1000);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(500000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(300);
        assertThat(planFeature.getCapacitySize().getValue().toBytes())
            .isEqualTo((210 * 1024 * 1024 * 1024L) + (300 * 1024 * 1024L));
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(200);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(180);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(180);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(20000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isTrue();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(6000L);
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(100000L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isTrue();

    }

    /**
     * Product Channel - DingTalk Billing: DingTalk Basic Edition Products
     */
    @Test
    public void testGetFreeProductByDingtalkChannel() {
        Product freeProduct = BillingConfigManager.getCurrentFreeProduct(ProductChannel.DINGTALK);
        assertThat(freeProduct).isNotNull();
        assertThat(ProductEnum.DINGTALK_BASE.getName()).isEqualTo(freeProduct.getId());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(freeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(30);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(20);
        assertThat(planFeature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1);
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(3);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(1000);
        assertThat(planFeature.getRainbowLabel().getValue()).isFalse();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(2L);
        assertThat(planFeature.getMessageCreditNums().getValue()).isZero();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(100L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(30L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();
    }

    /**
     * Product Channels - Feishu Billing: Feishu Basic Edition Products
     */
    @Test
    public void testGetFreeProductByFeishuChannel() {
        Product freeProduct = BillingConfigManager.getCurrentFreeProduct(ProductChannel.LARK);
        assertNotNull(freeProduct);
        assertEquals(freeProduct.getId(), ProductEnum.FEISHU_BASE.getName());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(freeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(30);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(20);
        assertThat(planFeature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1);
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(3);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(1000);
        assertThat(planFeature.getRainbowLabel().getValue()).isFalse();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(2L);
        assertThat(planFeature.getMessageCreditNums().getValue()).isZero();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(100L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(30L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();

    }

    /**
     * Product Channel-Enterprise WeChat Billing: Enterprise WeChat Basic Edition Products
     */
    @Test
    public void testGetFreeProductByWecomChannel() {
        Product freeProduct = BillingConfigManager.getCurrentFreeProduct(ProductChannel.WECOM);
        assertNotNull(freeProduct);
        assertEquals(freeProduct.getId(), ProductEnum.WECOM_BASE.getName());
        Map<String, Plan> planMap =
            MapUtil.getAny(BillingConfigManager.getBillingConfig().getPlans(),
                ArrayUtil.toArray(freeProduct.getPlans(), String.class));
        assertThat(planMap).isNotEmpty().hasSize(1);
        Plan freePlan = planMap.get(planMap.keySet().stream().findFirst().get());
        SubscriptionFeature planFeature = buildPlanFeature(freePlan, Collections.emptyList());
        assertThat(planFeature.getSeat().getValue()).isEqualTo(-1);
        assertThat(planFeature.getFileNodeNums().getValue()).isEqualTo(30);
        assertThat(planFeature.getApiCallNumsPerMonth().getValue()).isEqualTo(10000);
        assertThat(planFeature.getGanttViewNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getCalendarViewNums().getValue()).isEqualTo(5);
        assertThat(planFeature.getFormNums().getValue()).isEqualTo(20);
        assertThat(planFeature.getCapacitySize().getValue().toGigabytes()).isEqualTo(1);
        assertThat(planFeature.getAdminNums().getValue()).isEqualTo(3);
        assertThat(planFeature.getGalleryViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getNodePermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getFieldPermissionNums().getValue()).isEqualTo(10);
        assertThat(planFeature.getKanbanViewNums().getValue()).isEqualTo(-1);
        assertThat(planFeature.getRemainTimeMachineDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getRemainTrashDays().getValue()).isEqualTo(14);
        assertThat(planFeature.getTotalRows().getValue()).isEqualTo(20000);
        assertThat(planFeature.getRowsPerSheet().getValue()).isEqualTo(5000);
        assertThat(planFeature.getArchivedRowsPerSheet().getValue()).isEqualTo(1000);
        assertThat(planFeature.getSocialConnect().getValue()).isFalse();
        assertThat(planFeature.getRainbowLabel().getValue()).isFalse();
        assertThat(planFeature.getWatermark().getValue()).isFalse();
        assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(2L);
        assertThat(planFeature.getMessageCreditNums().getValue()).isZero();
        assertThat(planFeature.getAutomationRunNumsPerMonth().getValue())
            .isEqualTo(100L);
        assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(30L);
        assertThat(planFeature.getAuditQuery().getValue()).isFalse();
        assertThat(planFeature.getControlFormBrandLogo().getValue()).isFalse();
    }

    @Test
    public void testGetFreePlanByChannel() {
        // Sass: Bronze Edition
        Plan bronzePlan = BillingConfigManager.getFreePlan(ProductChannel.VIKA);
        assertNotNull(bronzePlan);
        assertEquals(bronzePlan.getProduct(), ProductEnum.BRONZE.getName());
        //DingTalk Billing: DingTalk Basic Edition
        Plan dingtalkBasePlan = BillingConfigManager.getFreePlan(ProductChannel.DINGTALK);
        assertNotNull(dingtalkBasePlan);
        assertEquals(dingtalkBasePlan.getProduct(), ProductEnum.DINGTALK_BASE.getName());
        // Feishu Billing: Feishu Basic Edition
        Plan feishuBasePlan = BillingConfigManager.getFreePlan(ProductChannel.LARK);
        assertNotNull(feishuBasePlan);
        assertEquals(feishuBasePlan.getProduct(), ProductEnum.FEISHU_BASE.getName());
        // Enterprise WeChat Billing: Enterprise WeChat Basic Edition
        Plan wecomBasePlan = BillingConfigManager.getFreePlan(ProductChannel.WECOM);
        assertNotNull(wecomBasePlan);
        assertEquals(wecomBasePlan.getProduct(), ProductEnum.WECOM_BASE.getName());
        // Proprietary Cloud Billing: Proprietary Cloud Ultimate
        Plan privateCloudPlan = BillingConfigManager.getFreePlan(ProductChannel.PRIVATE);
        assertNotNull(privateCloudPlan);
        assertEquals(privateCloudPlan.getProduct(), ProductEnum.PRIVATE_CLOUD.getName());
    }

    @Test
    public void testGetPriceBySeatAndMonth() {
        Price price = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 1);
        assertNotNull(price);
        assertEquals(price.getSeat(), 100);
        assertEquals(price.getMonth(), 1);
    }

    @Test
    public void testSilverPriceList() {
        List<Price> priceList = BillingConfigManager.getPriceList(ProductEnum.SILVER);
        // What plans are must-haves for the silver package
        Integer[] months = array(1, 6, 12);
        Integer[] seats = array(2, 100);
        assertEquals(priceList.size(), months.length * seats.length);
    }

    @Test
    public void testGoldPriceList() {
        List<Price> priceList = BillingConfigManager.getPriceList(ProductEnum.GOLD);
        // What plans are must-haves for the silver package
        Integer[] months = array(1, 6, 12);
        Integer[] seats = array(200);
        assertEquals(priceList.size(), months.length * seats.length);
    }

    @Test
    public void testEventConfigIsNotNull() {
        Map<String, Event> eventConfig = BillingConfigManager.getBillingConfig().getEvents();
        assertNotNull(eventConfig);
    }

    @Test
    public void testPriceListConfigIsNotNull() {
        Map<String, PriceList> priceListConfig =
            BillingConfigManager.getBillingConfig().getPricelist();
        assertNotNull(priceListConfig);
    }

    @Test
    public void testGetEventOnEffectiveDate() {
        assertThat(
            BillingConfigManager.getEventOnEffectiveDate(LocalDate.of(2022, 10, 23))).isNull();
        assertThat(
            BillingConfigManager.getEventOnEffectiveDate(LocalDate.of(2022, 10, 24))).isNotNull();
        assertThat(
            BillingConfigManager.getEventOnEffectiveDate(LocalDate.of(2022, 11, 11))).isNotNull();
        assertThat(
            BillingConfigManager.getEventOnEffectiveDate(LocalDate.of(2022, 11, 12))).isNull();
    }

    @Test
    public void testEnterpriseApiQps() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.ENTERPRISE.getName())) {
                assertTrue(plan.getFeatures().contains("api_qps_20"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(20);
            }
        });
    }

    @Test
    public void testGoldApiQps() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.GOLD.getName())) {
                assertTrue(plan.getFeatures().contains("api_qps_10"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(10);
            }
        });
    }

    @Test
    public void testSilverApiQps() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.SILVER.getName())) {
                assertTrue(plan.getFeatures().contains("api_qps_5"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(5);
            }
        });
    }

    @Test
    public void testBronzeApiQps() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.BRONZE.getName())) {
                assertTrue(plan.getFeatures().contains("api_qps_1"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getApiQpsNums().getValue()).isEqualTo(2L);
            }
        });
    }

    @Test
    public void testBronzeMessageCreditNums() {
        Plan plan = BillingConfigManager.getFreePlan(ProductChannel.VIKA);
        SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
        assertThat(planFeature.getMessageCreditNums().getValue()).isZero();
    }

    @Test
    public void testSilver2MessageCreditNums() {
        Plan plan = BillingConfigManager.getPlan(ProductEnum.SILVER, 2);
        SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(200L);
    }

    @Test
    public void testSilver100MessageCreditNums() {
        Plan plan = BillingConfigManager.getPlan(ProductEnum.SILVER, 100);
        SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(10000L);
    }

    @Test
    public void testGoldMessageCreditNums() {
        Plan plan = BillingConfigManager.getPlan(ProductEnum.GOLD, 200);
        SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(40000L);
    }

    @Test
    public void testEnterpriseMessageCreditNums() {
        Plan plan = BillingConfigManager.getPlan(ProductEnum.ENTERPRISE, 10);
        SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
        assertThat(planFeature.getMessageCreditNums().getValue()).isEqualTo(12000L);
    }

    @Test
    public void testBronzeMessageAutomationRunsAndWidget() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.BRONZE.getName())) {
                assertTrue(plan.getFeatures().contains("message_automation_run_100"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(100L);

                assertTrue(plan.getFeatures().contains("message_widget_30"));
                assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(30L);
            }
        });
    }

    @Test
    public void testSilverMessageAutomationRunsAndWidget() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.SILVER.getName())) {
                assertTrue(plan.getFeatures().contains("message_automation_run_10000"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(10000L);

                assertTrue(plan.getFeatures().contains("message_widget_unlimited"));
                assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
            }
        });
    }

    @Test
    public void testGoldMessageAutomationRunsAndWidget() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.GOLD.getName())) {
                assertTrue(plan.getFeatures().contains("message_automation_run_100000"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(
                    100000L);

                assertTrue(plan.getFeatures().contains("message_widget_unlimited"));
                assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
            }
        });
    }

    @Test
    public void testEnterpriseMessageAutomationRunsAndWidget() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.ENTERPRISE.getName())) {
                assertTrue(plan.getFeatures().contains("message_automation_run_500000"));
                SubscriptionFeature planFeature = buildPlanFeature(plan, Collections.emptyList());
                assertThat(planFeature.getAutomationRunNumsPerMonth().getValue()).isEqualTo(
                    500000L);

                assertTrue(plan.getFeatures().contains("message_widget_unlimited"));
                assertThat(planFeature.getWidgetNums().getValue()).isEqualTo(-1);
            }
        });
    }
}
