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

package com.apitable.enterprise.ops.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ops.ro.AutomationActionTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationActionTypeEditRO;
import com.apitable.enterprise.ops.ro.AutomationServiceCreateRO;
import com.apitable.enterprise.ops.ro.AutomationServiceEditRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeEditRO;
import com.apitable.enterprise.ops.ro.OpsAuthRo;
import com.apitable.enterprise.ops.service.IOpsAutomationService;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Automation API.
 * </p>
 */
@RestController
@ApiResource(path = "/ops/automation")
@Tag(name = "Product Operation System - Automation API")
public class OpsAutomationController {

    @Resource
    private IOpsService iOpsService;

    @Resource
    private IOpsAutomationService iOpsAutomationService;

    /**
     * Create Service.
     */
    @PostResource(path = "/services", requiredPermission = false)
    @Operation(summary = "Create Service")
    public ResponseData<String> createService(@RequestBody @Valid AutomationServiceCreateRO body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        return ResponseData.success(iOpsAutomationService.createService(userId, body));
    }

    /**
     * Edit Service.
     */
    @PostResource(path = "/services/{serviceId}", method = RequestMethod.PATCH,
        requiredPermission = false)
    @Operation(summary = "Edit Service")
    public ResponseData<Void> editService(@PathVariable("serviceId") String serviceId,
                                          @RequestBody AutomationServiceEditRO body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.editService(userId, serviceId, body);
        return ResponseData.success();
    }

    /**
     * Delete Service.
     */
    @PostResource(path = "/services/{serviceId}", method = RequestMethod.DELETE,
        requiredPermission = false)
    @Operation(summary = "Delete Service")
    public ResponseData<Void> deleteService(@PathVariable("serviceId") String serviceId,
                                            @RequestBody OpsAuthRo body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.deleteService(userId, serviceId);
        return ResponseData.success();
    }

    /**
     * Create Trigger Type.
     */
    @PostResource(path = "/trigger-types", requiredPermission = false)
    @Operation(summary = "Create Trigger Type")
    public ResponseData<String> createTriggerType(
        @RequestBody @Valid AutomationTriggerTypeCreateRO body
    ) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        return ResponseData.success(iOpsAutomationService.createTriggerType(userId, body));
    }

    /**
     * Edit Trigger Type.
     */
    @PostResource(path = "/trigger-types/{triggerTypeId}", method = RequestMethod.PATCH,
        requiredPermission = false)
    @Operation(summary = "Edit Trigger Type")
    public ResponseData<Void> editTriggerType(@PathVariable("triggerTypeId") String triggerTypeId,
                                              @RequestBody AutomationTriggerTypeEditRO body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.editTriggerType(userId, triggerTypeId, body);
        return ResponseData.success();
    }

    /**
     * Delete Trigger Type.
     */
    @PostResource(path = "/trigger-types/{triggerTypeId}", method = RequestMethod.DELETE,
        requiredPermission = false)
    @Operation(summary = "Delete Trigger Type")
    public ResponseData<Void> deleteTriggerType(@PathVariable("triggerTypeId") String triggerTypeId,
                                                @RequestBody OpsAuthRo body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.deleteTriggerType(userId, triggerTypeId);
        return ResponseData.success();
    }

    /**
     * Create Action Type.
     */
    @PostResource(path = "/action-types", requiredPermission = false)
    @Operation(summary = "Create Action Type")
    public ResponseData<String> createActionType(
        @RequestBody @Valid AutomationActionTypeCreateRO body
    ) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        return ResponseData.success(iOpsAutomationService.createActionType(userId, body));
    }

    /**
     * Edit Action Type.
     */
    @PostResource(path = "/action-types/{actionTypeId}", method = RequestMethod.PATCH,
        requiredPermission = false)
    @Operation(summary = "Edit Action Type")
    public ResponseData<Void> editActionType(@PathVariable("actionTypeId") String actionTypeId,
                                             @RequestBody AutomationActionTypeEditRO body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.editActionType(userId, actionTypeId, body);
        return ResponseData.success();
    }

    /**
     * Delete Action Type.
     */
    @PostResource(path = "/action-types/{actionTypeId}", method = RequestMethod.DELETE,
        requiredPermission = false)
    @Operation(summary = "Delete Action Type")
    public ResponseData<Void> deleteActionType(@PathVariable("actionTypeId") String actionTypeId,
                                               @RequestBody OpsAuthRo body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsAutomationService.deleteActionType(userId, actionTypeId);
        return ResponseData.success();
    }

}
