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

package com.apitable.enterprise.gm.controller;

import static com.apitable.workspace.enums.PermissionException.NODE_OPERATION_DENIED;

import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.ro.GlobalWidgetListRo;
import com.apitable.enterprise.gm.ro.SingleGlobalWidgetRo;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.gm.service.IWidgetGmService;
import com.apitable.enterprise.widget.ro.WidgetPackageBanRo;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.holder.SpaceHolder;
import com.apitable.widget.vo.GlobalWidgetInfo;
import com.apitable.workspace.service.INodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Used for GM command in vika-cli command line tool.(the global widget).
 * </p>
 */
@RestController
@Tag(name = "GM Widget API")
@ApiResource
@Slf4j
public class GmGlobalWidgetController {

    @Resource
    private IWidgetGmService iWidgetGmService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ControlTemplate controlTemplate;

    @Resource
    private IGmService iGmService;

    /**
     * Gets a list of global widget stores.
     */
    @PostResource(path = "/gm/widget/global/list", requiredPermission = false)
    @Operation(summary = "Gets a list of global widget stores")
    public ResponseData<List<GlobalWidgetInfo>> globalWidgetList(
        @RequestBody @Valid GlobalWidgetListRo globalWidgetRo) {
        Long userId = SessionContext.getUserId();
        String nodeId = globalWidgetRo.getNodeId();
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        SpaceHolder.set(spaceId);
        // Verify whether the user exist the space.
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // Check whether the user have permission to view the information
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));
        return ResponseData.success(iWidgetGmService.getGlobalWidgetPackageConfiguration(nodeId,
            globalWidgetRo.getViewId()));
    }

    /**
     * Refresh the global component DB data.
     */
    @PostResource(path = "/gm/widget/global/refresh/db", requiredPermission = false)
    @Operation(summary = "Refresh the global component DB data")
    public ResponseData<Void> globalWidgetDbDataRefresh(
        @RequestBody @Valid GlobalWidgetListRo globalWidgetRo) {
        Long userId = SessionContext.getUserId();
        String nodeId = globalWidgetRo.getNodeId();
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        SpaceHolder.set(spaceId);
        // Verify whether the user exist the space.
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // Check whether the user have permission to view the information
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));
        iWidgetGmService.globalWidgetDbDataRefresh(nodeId, globalWidgetRo.getViewId());
        return ResponseData.success();
    }

    /**
     * Refresh the data of a single widget(the robot calls).
     */
    @PostResource(path = "/gm/widget/global/refresh/single", requiredPermission = false)
    @Operation(summary = "Refresh the data of a single widget(the robot calls)", hidden = true)
    public ResponseData<Void> singleGlobalWidgetRefresh(
        @RequestBody @Valid SingleGlobalWidgetRo body) {
        Long userId = SessionContext.getUserId();
        String nodeId = body.getNodeId();
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        SpaceHolder.set(spaceId);
        // Verify whether the user exist the space.
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // Check whether the user have permission to view the information
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));
        iWidgetGmService.singleGlobalWidgetRefresh(body);
        return ResponseData.success();
    }

    /**
     * Ban/Unban widget.
     */
    @PostResource(path = "/widget/package/ban", requiredPermission = false)
    @Operation(summary = "Ban/Unban widget", description = "widget-cli ban/unban widget")
    @Parameters({
        @Parameter(name = HttpHeaders.AUTHORIZATION, description = "developer token", required =
            false, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<Void> banWidget(@RequestBody @Valid WidgetPackageBanRo widget) {
        Long userId = SessionContext.getUserId();
        // verify operation permissions
        iGmService.validPermission(userId,
            Boolean.TRUE.equals(widget.getUnban()) ? GmAction.WIDGET_UNBAN : GmAction.WIDGET_BAN);
        iWidgetGmService.banWidget(userId, widget);
        return ResponseData.success();
    }

}
