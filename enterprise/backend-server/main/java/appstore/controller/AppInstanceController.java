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

package com.apitable.enterprise.appstore.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.CreateAppInstance;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application management library interface.
 */
@RestController
@Tag(name = "App Store - Instance")
@ApiResource(path = "/")
public class AppInstanceController {

    @Resource
    private IAppInstanceService iAppInstanceService;

    /**
     * Query the application instance list.
     */
    @GetResource(path = "/appInstances", requiredPermission = false)
    @Operation(summary = "Query the application instance list", description = "At present, the "
        + "interface is full query, and the paging query function will be provided later, so you "
        + "don't need to pass paging parameters")
    @Parameters({
        @Parameter(name = "spaceId", description = "Space ID", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.PATH, example = "spc123456"),
        @Parameter(name = "pageIndex", description = "Page Index", schema = @Schema(type =
            "string"),
            in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "pageSize", description = "Quantity per page", schema = @Schema(type =
            "string"), in = ParameterIn.QUERY, example = "50"),
        @Parameter(name = "orderBy", description = "Sort field", schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = "createdAt"),
        @Parameter(name = "sortBy", description = "Collation,asc=positive sequence,desc=Reverse "
            + "order", schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "desc")
    })
    public ResponseData<PageInfo<AppInstance>> fetchAppInstances(
        @RequestParam("spaceId") String spaceId,
        @RequestParam(name = "pageIndex", required = false, defaultValue = "1") Integer pageIndex,
        @RequestParam(name = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        // Synchronize application market data
        iAppInstanceService.compatibleMarketPlace(spaceId);
        List<AppInstance> appInstances = iAppInstanceService.getAppInstancesBySpaceId(spaceId);
        return ResponseData.success(
            PageHelper.build(pageIndex, pageSize, appInstances.size(), appInstances));
    }

    /**
     * Get the configuration of a single application instance.
     */
    @GetResource(path = "/appInstances/{appInstanceId}", requiredPermission = false)
    @Operation(summary = "Get the configuration of a single application instance", description =
        "Get "
            + "the configuration according to the application instance ID")
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-xxxxx"),
    })
    public ResponseData<AppInstance> getAppInstance(
        @PathVariable("appInstanceId") String appInstanceId) {
        return ResponseData.success(iAppInstanceService.getAppInstance(appInstanceId));
    }

    /**
     * Create an application instance.
     */
    @PostResource(path = "/appInstances", requiredPermission = false)
    @Operation(summary = "Create an application instance", description = "Opening an application "
        + "instance")
    public ResponseData<AppInstance> createAppInstance(@RequestBody @Valid CreateAppInstance data) {
        return ResponseData.success(
            iAppInstanceService.createInstance(data.getSpaceId(), data.getAppId()));
    }

    /**
     * Enable apps.
     */
    @PostResource(path = "/appInstances/{appInstanceId}/enable", requiredPermission = false)
    @Operation(summary = "Enable apps", description =
        "When the application instance is disabled, the "
            + "space station re enables the application", hidden = true)
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-xxxxx"),
    })
    public ResponseData<Void> enable(@PathVariable("appInstanceId") String appInstanceId) {
        iAppInstanceService.updateAppInstanceStatus(appInstanceId, true);
        return ResponseData.success();
    }

    @PostResource(path = "/appInstances/{appInstanceId}/disable", requiredPermission = false)
    @Operation(summary = "Deactivate app", description =
        "The space station actively deactivates the "
            + "application", hidden = true)
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-xxxxx"),
    })
    public ResponseData<Void> disable(@PathVariable("appInstanceId") String appInstanceId) {
        iAppInstanceService.updateAppInstanceStatus(appInstanceId, false);
        return ResponseData.success();
    }

    /**
     * Delete app.
     */
    @PostResource(path = "/appInstances/{appInstanceId}", method = RequestMethod.DELETE,
        requiredPermission = false)
    @Operation(summary = "Delete app", description = "The space actively deletes applications")
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-xxxxx"),
    })
    public ResponseData<Void> delete(@PathVariable("appInstanceId") String appInstanceId) {
        Long userId = SessionContext.getUserId();
        iAppInstanceService.deleteAppInstance(userId, appInstanceId);
        return ResponseData.success();
    }
}
