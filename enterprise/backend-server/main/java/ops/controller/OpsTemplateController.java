/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License
 *  and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory
 * and its subdirectories does not constitute permission to use this code
 * or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.ops.controller;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ops.ro.TemplateCategoryCreateRo;
import com.apitable.enterprise.ops.ro.TemplatePublishRo;
import com.apitable.enterprise.ops.ro.TemplateUnpublishRo;
import com.apitable.enterprise.ops.service.IOpsTemplateService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.context.LoginContext;
import com.apitable.template.service.ITemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Template API.
 * </p>
 */
@RestController
@ApiResource(path = "/ops")
@Tag(name = "Product Operation System - Template API")
public class OpsTemplateController {

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private IOpsTemplateService iOpsTemplateService;

    @Resource
    private ConstProperties constProperties;

    @PostResource(path = "/templateCategory/create", requiredPermission = false)
    @Operation(summary = "Create Template Category",
        description = "Only supply to people in template space")
    public ResponseData<String> createTemplateCategory(
        @RequestBody @Valid TemplateCategoryCreateRo data) {
        // check if the user is in the space
        String templateSpaceId = constProperties.getTemplateSpace();
        LoginContext.me().getUserSpaceDto(templateSpaceId);
        // create template category
        return ResponseData.success(iOpsTemplateService.createTemplateCategory(data));
    }

    @PostResource(path = "/templateCategories/{categoryCode}",
        requiredPermission = false, method = RequestMethod.DELETE)
    @Operation(summary = "Delete Template Category",
        description = "Only supply to people in template space")
    public ResponseData<Void> deleteTemplateCategory(
        @PathVariable("categoryCode") String categoryCode) {
        // check if the user is in the space
        String templateSpaceId = constProperties.getTemplateSpace();
        LoginContext.me().getUserSpaceDto(templateSpaceId);
        // delete template category
        iOpsTemplateService.deleteTemplateCategory(categoryCode);
        return ResponseData.success();
    }

    @PostResource(path = "/templates/{templateId}/publish", requiredPermission = false)
    @Operation(summary = "Publish Template in Specified Template Category",
        description = "Only supply to people in template space")
    @Parameter(name = "templateId", description = "template id", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "tplxxx")
    public ResponseData<Void> publish(@PathVariable("templateId") String templateId,
                                      @RequestBody @Valid TemplatePublishRo data) {
        this.checkPermissionAndTemplateSpace(templateId);
        // publish template
        iOpsTemplateService.publishTemplate(templateId, data);
        return ResponseData.success();
    }

    @PostResource(path = "/templates/{templateId}/unpublish", requiredPermission = false)
    @Operation(summary = "UnPublish Template",
        description = "Only supply to people in template space")
    @Parameter(name = "templateId", description = "template id", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "tplxxx")
    public ResponseData<Void> unpublish(@PathVariable("templateId") String templateId,
                                        @RequestBody @Valid TemplateUnpublishRo data) {
        this.checkPermissionAndTemplateSpace(templateId);
        // unpublish template
        iOpsTemplateService.unpublishTemplate(templateId, data);
        return ResponseData.success();
    }

    private void checkPermissionAndTemplateSpace(String templateId) {
        // check if the user is in the space
        String templateSpaceId = constProperties.getTemplateSpace();
        LoginContext.me().getUserSpaceDto(templateSpaceId);
        // check template exist and space
        String spaceId = iTemplateService.getSpaceId(templateId);
        if (!spaceId.equals(templateSpaceId)) {
            throw new BusinessException("This template does not exist in the template space.");
        }
    }

}
