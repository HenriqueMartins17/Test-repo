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
import com.apitable.enterprise.ops.service.ITemplateCenterConfigService;
import com.apitable.enterprise.ops.ro.OpsWizardRo;
import com.apitable.enterprise.ops.ro.TemplateAssetRemarkRo;
import com.apitable.enterprise.ops.ro.TemplateCenterConfigRo;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System API.
 * </p>
 */
@Slf4j
@RestController
@ApiResource(path = "/ops")
@Tag(name = "Product Operation System API")
public class OpsController {

    @Resource
    private IOpsService iOpsService;

    @Resource
    private ITemplateCenterConfigService iTemplateCenterConfigService;

    /**
     * Update Template Center Config.
     */
    @PostResource(path = "/template/config", requiredPermission = false)
    @Operation(summary = "Update Template Center Config")
    public ResponseData<Void> config(@RequestBody @Valid TemplateCenterConfigRo body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iTemplateCenterConfigService.updateTemplateCenterConfig(userId, body);
        return ResponseData.success();
    }

    /**
     * Template Asset Remark.
     */
    @PostResource(path = "/templates/{templateId}/asset/mark", requiredLogin = false)
    @Operation(summary = "Template Asset Remark", description = "Indicates the attachment "
        + "resource of the specified template. Users refer to this part of the resource without "
        + "occupying the space station capacity")
    @Parameter(name = "templateId", description = "Template Custom ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "tpcE7fyADP99W")
    public ResponseData<Void> markTemplateAsset(@PathVariable("templateId") String templateId,
                                                @RequestBody TemplateAssetRemarkRo body) {
        Boolean isReversed = body.getIsReversed();
        log.info("Operator 「{}」 {} marks the asset of template「{}」",
            SessionContext.getUserIdWithoutException(), isReversed ? "reverse" : null, templateId);
        // check permission
        iOpsService.auth(body.getToken());
        // mark template asset
        iOpsService.markTemplateAsset(templateId, isReversed);
        return ResponseData.success();
    }

    /**
     * General configuration.
     */
    @PostResource(path = "/wizard", requiredPermission = false)
    @Operation(summary = "Save or Update Wizard Configuration")
    public ResponseData<Void> saveOrUpdateWizard(@RequestBody @Valid OpsWizardRo body) {
        iOpsService.auth(body.getToken());
        Long userId = SessionContext.getUserId();
        iOpsService.saveOrUpdateWizard(userId, body);
        return ResponseData.success();
    }

}
