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
import com.apitable.enterprise.appstore.model.MarketplaceSpaceAppVo;
import com.apitable.enterprise.appstore.service.IMarketplaceAppService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Application Market - Application API.
 * </p>
 */
@RestController
@Tag(name = "Marketplace - Application")
@ApiResource(path = "/marketplace/integration")
public class MarketplaceAppController {

    @Resource
    private IMarketplaceAppService iMarketplaceAppService;

    @GetResource(path = "/space/{spaceId}/apps", requiredLogin = false)
    @Operation(summary = "Query Built-in Integrated Applications")
    @Deprecated
    public ResponseData<List<MarketplaceSpaceAppVo>> getSpaceAppList(
        @PathVariable("spaceId") String spaceId) {
        return ResponseData.success(iMarketplaceAppService.getSpaceAppList(spaceId));
    }

    @PostResource(path = "/space/{spaceId}/app/{appId}/open", requiredPermission = false)
    @Operation(summary = "Open Application")
    @Deprecated
    public ResponseData<Void> openSpaceApp(@PathVariable("spaceId") String spaceId,
                                           @PathVariable(name = "appId") String appId) {
        iMarketplaceAppService.openSpaceApp(spaceId, appId);
        return ResponseData.success();
    }

    @PostResource(path = "/space/{spaceId}/app/{appId}/stop", requiredPermission = false)
    @Operation(summary = "Block Application")
    @Deprecated
    public ResponseData<Void> blockSpaceApp(@PathVariable("spaceId") String spaceId,
                                            @PathVariable(name = "appId") String appId) {
        iMarketplaceAppService.stopSpaceApp(spaceId, appId);
        return ResponseData.success();
    }
}
