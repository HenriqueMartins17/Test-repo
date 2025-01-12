/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.automation.controller;

import static com.apitable.automation.enums.AutomationException.DST_ROBOT_LIMIT;
import static com.apitable.workspace.enums.PermissionException.NODE_OPERATION_DENIED;

import cn.hutool.core.util.StrUtil;
import com.apitable.automation.model.AutomationRobotDto;
import com.apitable.automation.service.IAutomationRobotService;
import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.automation.model.AutomationApiTriggerCreateRo;
import com.apitable.enterprise.automation.model.AutomationTriggerCreateVo;
import com.apitable.enterprise.automation.service.IAutomationOpenService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.LimitProperties;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.holder.SpaceHolder;
import com.apitable.workspace.service.INodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Automation - Open API")
@ApiResource(path = "/automation/open/triggers")
public class AutomationOpenApiController {

    @Resource
    private IAutomationOpenService iAutomationOpenService;

    @Resource
    private IAutomationRobotService iAutomationRobotService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private ControlTemplate controlTemplate;

    @Resource
    private LimitProperties limitProperties;

    @PostResource(path = "/createOrUpdate", requiredPermission = false)
    @Operation(description = "Create/Update trigger and robot")
    @Parameter(name = ParamsConstants.X_SERVICE_TOKEN, description = "Service Provider Auth Token",
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "asvDsF724qvkLdd83J")
    public ResponseData<AutomationTriggerCreateVo> createOrUpdateTrigger(
        @RequestBody @Valid AutomationApiTriggerCreateRo data,
        @RequestHeader(name = ParamsConstants.X_SERVICE_TOKEN, required = false)
        String xServiceToken
    ) {
        // todo: verify Service Provider Auth Token
        if (StrUtil.isEmpty(xServiceToken)) {
            return ResponseData.status(false, 500, "X-Service-Token no allow null").data(null);
        }
        // Whether the user has management permission.
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getRobot().getResourceId());
        SpaceHolder.set(spaceId);
        // Method includes determining whether the user is in this space.
        Long memberId = LoginContext.me().getUserSpaceDto(spaceId).getMemberId();
        // Verify whether the user has the specified node operation permission.
        controlTemplate.checkNodePermission(memberId,
            data.getRobot().getResourceId(), NodePermission.MANAGE_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));
        // The robot cannot be created if the number of robots in a datasheet more than limitations.
        List<AutomationRobotDto> automationRobotDtoList =
            iAutomationRobotService.getRobotListByResourceId(data.getRobot().getResourceId());
        ExceptionUtil.isFalse(automationRobotDtoList.size()
            >= limitProperties.getDstRobotMaxCount(), DST_ROBOT_LIMIT);

        return iAutomationOpenService.upsert(data, xServiceToken);
    }

    @PostResource(path = "/datasheets/{datasheetId}/robots",
        requiredPermission = false, method = RequestMethod.DELETE)
    @Operation(description = "Delete trigger and robot")
    @Parameter(name = ParamsConstants.X_SERVICE_TOKEN, description = "Service Provider Auth Token",
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "asvDsF724qvkLdd83J")
    public ResponseData<String> deleteTrigger(
        @PathVariable(name = "datasheetId") String datasheetId,
        @RequestParam(name = "robotIds") String[] robotIds
    ) {
        // todo: verify Service Provider Auth Token
        if (robotIds.length == 0) {
            return ResponseData.success("robotIds[] no allow null");
        }
        // Verify permission.
        // Method includes determining whether the node is in this space.
        String spaceId = iNodeService.getSpaceIdByNodeId(datasheetId);
        SpaceHolder.set(spaceId);
        // Method includes determining whether the user is in this space.
        Long memberId = LoginContext.me().getUserSpaceDto(spaceId).getMemberId();
        // Verify whether the user has the specified node operation permission.
        controlTemplate.checkNodePermission(memberId, datasheetId, NodePermission.MANAGE_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));

        // Query the existing robots.
        List<AutomationRobotDto> automationRobotDtoList =
            iAutomationRobotService.getRobotListByResourceId(datasheetId);
        if (automationRobotDtoList == null || automationRobotDtoList.size() == 0) {
            return ResponseData.success("Datasheet hasn't robots.");
        }
        List<String> robotList = automationRobotDtoList.stream()
            .map(AutomationRobotDto::getRobotId).collect(Collectors.toList());
        robotList.retainAll(Arrays.asList(robotIds));
        iAutomationRobotService.delete(robotList);
        return ResponseData.success("");
    }

}
