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

package com.apitable.enterprise.document.controller;

import static com.apitable.enterprise.document.enums.DocumentException.DOCUMENT_NOT_EXIST;

import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.document.model.DocumentView;
import com.apitable.enterprise.document.service.IDocumentService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workbench - Document Api.
 */
@RestController
@ApiResource(path = "/documents")
@Tag(name = "Workbench - Document Api")
public class DocumentController {

    @Resource
    private IDocumentService iDocumentService;

    @GetResource(path = "/new-name", requiredPermission = false)
    @Operation(summary = "Get new document name")
    public ResponseData<String> getNewDocumentName() {
        return ResponseData.success(iDocumentService.getNewDocumentName());
    }

    @GetResource(path = "/{documentName}", requiredPermission = false)
    @Operation(summary = "Get document information")
    @Parameter(name = "documentName", in = ParameterIn.PATH, required = true,
        schema = @Schema(type = "string"), example = "docoacp1Q48Xq")
    public ResponseData<DocumentView> getDocument(@PathVariable("documentName") String name) {
        Long userId = SessionContext.getUserId();
        String spaceId = iDocumentService.getSpaceIdByDocumentName(name, false);
        ExceptionUtil.isNotNull(spaceId, DOCUMENT_NOT_EXIST);
        // check if space is spanned
        LoginContext.me().checkAcrossSpace(userId, spaceId);
        return ResponseData.success(iDocumentService.getDocumentView(name));
    }
}
