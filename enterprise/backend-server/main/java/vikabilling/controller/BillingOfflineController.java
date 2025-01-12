/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.ro.CreateBusinessOrderRo;
import com.apitable.enterprise.gm.ro.CreateEntitlementWithAddOn;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.model.OfflineOrderInfo;
import com.apitable.enterprise.vikabilling.model.SpaceSubscriptionVo;
import com.apitable.enterprise.vikabilling.service.IBillingOfflineService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operating System - Subscription Billing Module.
 */
@RestController
@Tag(name = "Billing Internal Api")
@ApiResource(path = "/billing")
@Slf4j
public class BillingOfflineController {

    @Resource
    private IBillingOfflineService iBillingOfflineService;

    @Resource
    private IGmService iGmService;

    /**
     * Create Order.
     */
    @PostResource(path = "/orders", requiredPermission = false)
    @Operation(summary = "Create Order", hidden = true)
    public ResponseData<Void> createOrder(@RequestBody @Valid CreateBusinessOrderRo data) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.BILLING_ORDER_CREATE);
        // Create business order
        OfflineOrderInfo offlineOrderInfo =
            iBillingOfflineService.createBusinessOrder(userId, data);
        // Sync order events
        SpringContextHolder.getApplicationContext()
            .publishEvent(new SyncOrderEvent(this, offlineOrderInfo.getOrderId()));
        return ResponseData.success();
    }

    /**
     * Giveaway Add-on Plan.
     */
    @PostResource(path = "/createEntitlementWithAddOn", requiredPermission = false)
    @Operation(summary = "Giveaway Add-on Plan", hidden = true)
    public ResponseData<Void> reward(@RequestBody @Valid CreateEntitlementWithAddOn data) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.BILLING_ORDER_CREATE);
        iBillingOfflineService.createSubscriptionWithAddOn(data, userId);
        return ResponseData.success();
    }

    /**
     * Query Space for Orders.
     */
    @GetResource(path = "/space/{spaceId}/subscription", requiredPermission = false)
    @Operation(summary = "Query Space for Orders", hidden = true)
    public ResponseData<SpaceSubscriptionVo> fetchSpaceOrder(
        @PathVariable("spaceId") String spaceId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.BILLING_ORDER_QUERY);
        return ResponseData.success(iBillingOfflineService.getSpaceSubscription(spaceId));
    }
}
