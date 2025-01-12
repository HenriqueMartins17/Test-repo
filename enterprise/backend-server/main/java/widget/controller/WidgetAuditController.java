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

package com.apitable.enterprise.widget.controller;

import static com.apitable.workspace.enums.PermissionException.NODE_OPERATION_DENIED;

import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.FieldPermission;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.widget.ro.WidgetAuditGlobalIdRo;
import com.apitable.enterprise.widget.ro.WidgetAuditSubmitDataRo;
import com.apitable.enterprise.widget.service.IWidgetAuditService;
import com.apitable.enterprise.widget.vo.WidgetIssuedGlobalIdVo;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.workspace.service.INodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Widget SDK - Widget Audit Api.
 */
@RestController
@Tag(name = "Widget SDK - Widget Audit Api")
@ApiResource(path = "/widget/audit")
public class WidgetAuditController {

    @Resource
    private IWidgetAuditService iWidgetAuditService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ControlTemplate controlTemplate;

    /**
     * Issue global id.
     */
    @PostResource(path = "/issued/globalId", requiredPermission = false)
    @Operation(summary = "Issue global id")
    public ResponseData<WidgetIssuedGlobalIdVo> issuedGlobalId(
        @RequestBody @Valid WidgetAuditGlobalIdRo body) {
        Long userId = SessionContext.getUserId();
        String dstId = body.getDstId();
        String fieldId = body.getFieldId();

        // The method includes determining whether the template exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(dstId);
        // verify whether the user is in this space
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        controlTemplate.checkFieldPermission(memberId, dstId, fieldId,
            FieldPermission.EDIT_FIELD_DATA,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));

        WidgetIssuedGlobalIdVo result = new WidgetIssuedGlobalIdVo();
        result.setIssuedGlobalId(iWidgetAuditService.issuedGlobalId(userId, body));
        return ResponseData.success(result);
    }

    /**
     * Audit global widget submit data.
     */
    @PostResource(path = "/submit/data", requiredPermission = false)
    @Operation(summary = "Audit global widget submit data")
    public ResponseData<Void> auditSubmitData(@RequestBody @Valid WidgetAuditSubmitDataRo body) {
        Long userId = SessionContext.getUserId();
        String dstId = body.getDstId();
        String fieldId = body.getFieldId();

        // The method includes determining whether the template exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(dstId);
        // verify whether the user is in this space
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        controlTemplate.checkFieldPermission(memberId, dstId, fieldId,
            FieldPermission.EDIT_FIELD_DATA,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));

        iWidgetAuditService.auditSubmitData(userId, body);
        return ResponseData.success();
    }
}
