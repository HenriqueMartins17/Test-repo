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


import com.apitable.asset.ro.AttachOfficePreviewRo;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.appstore.service.IOfficeService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Office Operation API.
 */
@RestController
@Tag(name = "Office Suite")
@ApiResource
public class OfficeAppController {

    @Resource
    private IOfficeService iOfficeService;

    @PostResource(path = "/base/attach/officePreview/{spaceId}",
        requiredPermission = false)
    @Operation(summary = "Office document preview conversion",
        description = "Office document preview conversion,"
            + "call Yongzhong office conversion interface")
    public ResponseData<String> officePreview(@PathVariable String spaceId,
                                              @RequestBody @Valid AttachOfficePreviewRo results) {
        return ResponseData.success(iOfficeService.officePreview(results, spaceId));
    }
}
