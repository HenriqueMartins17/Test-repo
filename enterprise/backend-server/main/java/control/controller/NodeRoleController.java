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

package com.apitable.enterprise.control.controller;

import static com.apitable.shared.constants.PageConstants.PAGE_DESC;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;
import static java.util.stream.Collectors.toList;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.control.service.IControlService;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.organization.service.IOrganizationService;
import com.apitable.organization.service.IUnitService;
import com.apitable.organization.vo.UnitMemberVo;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.component.notification.annotation.Notification;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.AuditConstants;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.holder.SpaceHolder;
import com.apitable.shared.listener.event.AuditSpaceEvent;
import com.apitable.shared.listener.event.AuditSpaceEvent.AuditSpaceArg;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.apitable.space.enums.AuditSpaceAction;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.service.ISpaceRoleService;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.workspace.dto.ControlRoleInfo;
import com.apitable.workspace.enums.PermissionException;
import com.apitable.workspace.ro.AddNodeRoleRo;
import com.apitable.workspace.ro.BatchDeleteNodeRoleRo;
import com.apitable.workspace.ro.BatchModifyNodeRoleRo;
import com.apitable.workspace.ro.DeleteNodeRoleRo;
import com.apitable.workspace.ro.ModifyNodeRoleRo;
import com.apitable.workspace.ro.RoleControlOpenRo;
import com.apitable.workspace.service.INodeRoleService;
import com.apitable.workspace.service.INodeService;
import com.apitable.workspace.vo.NodeCollaboratorsVo;
import com.apitable.workspace.vo.NodeRoleMemberVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workbench - Node Role Api.
 */
@Tag(name = "Workbench - Node Role Api")
@RestController
@ApiResource(path = "/node")
public class NodeRoleController {

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IControlService iControlService;

    @Resource
    private INodeRoleService iNodeRoleService;

    @Resource
    private IOrganizationService iOrganizationService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private ControlTemplate controlTemplate;

    @Resource
    private ISpaceRoleService iSpaceRoleService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @GetResource(path = "/collaborator/page")
    @Operation(summary = "Page Query the Node' Collaborator", description = PAGE_DESC)
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "nodeId", description = "node id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "nodRTGSy43DJ9"),
        @Parameter(name = PAGE_PARAM, description = "page's parameter", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY,
            example = PAGE_SIMPLE_EXAMPLE)
    })
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<NodeRoleMemberVo>> getCollaboratorPage(
        @RequestParam(name = "nodeId") String nodeId, @PageObjectParam Page page
    ) {
        Long memberId = LoginContext.me().getMemberId();
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_ACCESS_DENIED));
        PageInfo pageInfo =
            iNodeRoleService.getNodeRoleMembersPageInfo(page, nodeId);
        return ResponseData.success(pageInfo);
    }

    /**
     * Get node roles.
     */
    @GetResource(path = "/listRole")
    @Operation(summary = "Get node roles")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "nodeId", description = "node id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "nodRTGSy43DJ9"),
        @Parameter(name = "includeAdmin", description = "Whether to include the master "
            + "administrator, can not be passed, the default includes", schema = @Schema(type =
            "boolean"), in = ParameterIn.QUERY, example = "true"),
        @Parameter(name = "includeSelf", description = "Whether to get userself, do not pass, the"
            + " default contains", schema = @Schema(type = "boolean"), in = ParameterIn.QUERY,
            example = "true"),
        @Parameter(name = "includeExtend", description = "Contains superior inherited permissions"
            + ". By default, it does not include", schema = @Schema(type = "boolean"), in =
            ParameterIn.QUERY, example = "false")
    })
    public ResponseData<NodeCollaboratorsVo> listRole(@RequestParam(name = "nodeId") String nodeId,
                                                      @RequestParam(name = "includeAdmin", defaultValue = "true")
                                                      Boolean includeAdmin,
                                                      @RequestParam(name = "includeSelf", defaultValue = "true")
                                                      Boolean includeSelf,
                                                      @RequestParam(name = "includeExtend", defaultValue = "false")
                                                      Boolean includeExtend) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // check whether you have permission to view
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        SpaceGlobalFeature feature = iSpaceService.getSpaceGlobalFeature(spaceId);
        SpaceHolder.setGlobalFeature(feature);
        NodeCollaboratorsVo collaboratorsVo = new NodeCollaboratorsVo();
        // query the permission mode of the node
        iControlService.checkControlStatus(nodeId, status -> collaboratorsVo.setExtend(!status));
        if (includeAdmin) {
            // query node administrator view
            List<Long> admins = iSpaceRoleService.getSpaceAdminsWithWorkbenchManage(spaceId);
            collaboratorsVo.setAdmins(iOrganizationService.findAdminsVo(admins, spaceId));
        }
        if (includeSelf) {
            // query user self
            List<UnitMemberVo> unitMemberVos =
                iOrganizationService.findUnitMemberVo(Collections.singletonList(memberId));
            collaboratorsVo.setSelf(
                CollUtil.isNotEmpty(unitMemberVos) ? CollUtil.getFirst(unitMemberVos) : null);
        }
        // query node role view
        if (includeExtend) {
            // check inherited roles
            String parentNodeId = iNodeRoleService.getNodeExtendNodeId(nodeId);
            if (parentNodeId == null) {
                // No parent node has enabled permissions, default workbench role, load root
                // department information
                collaboratorsVo.setRoleUnits(
                    Collections.singletonList(iNodeRoleService.getRootNodeRoleUnit(spaceId)));
            } else {
                // load parent node role
                collaboratorsVo.setOwner(iNodeRoleService.getNodeOwner(parentNodeId));
                collaboratorsVo.setRoleUnits(iNodeRoleService.getNodeRoleUnitList(parentNodeId));
            }
        } else {
            // automatically query roles in node permission mode
            if (collaboratorsVo.getExtend()) {
                // query the role of inheritance mode
                String parentNodeId = iNodeRoleService.getNodeExtendNodeId(nodeId);
                if (parentNodeId == null) {
                    // there is no permission to inherit the parent node
                    collaboratorsVo.setRoleUnits(
                        Collections.singletonList(iNodeRoleService.getRootNodeRoleUnit(spaceId)));
                    collaboratorsVo.setExtendNodeName(
                        spaceMapper.selectSpaceNameBySpaceId(spaceId));
                } else {
                    // load parent node role
                    collaboratorsVo.setOwner(iNodeRoleService.getNodeOwner(parentNodeId));
                    collaboratorsVo.setRoleUnits(
                        iNodeRoleService.getNodeRoleUnitList(parentNodeId));
                    collaboratorsVo.setExtendNodeName(
                        iNodeService.getNodeNameByNodeId(parentNodeId));
                }
            } else {
                // query the node role in the specified mode
                // inquire the person in charge
                collaboratorsVo.setOwner(iNodeRoleService.getNodeOwner(nodeId));
                collaboratorsVo.setRoleUnits(iNodeRoleService.getNodeRoleUnitList(nodeId));
            }
        }
        collaboratorsVo.setBelongRootFolder(iNodeService.isNodeBelongRootFolder(spaceId, nodeId));
        return ResponseData.success(collaboratorsVo);
    }

    /**
     * Disable role extend.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/disableRoleExtend")
    @Operation(summary = "Disable role extend")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "nodeId", description = "node id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "nodRTGSy43DJ9"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> disableRoleExtend(@RequestParam(name = "nodeId") String nodeId,
                                                @RequestBody(required = false)
                                                RoleControlOpenRo roleControlOpenRo) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(nodeId), PermissionException.NODE_OPERATION_DENIED);
        // check whether user have permission
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node must be inherited before it is turned off.
        iControlService.checkControlStatus(nodeId,
            status -> ExceptionUtil.isFalse(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // Enable whether node permissions inherit the default role organization unit list
        boolean includeExtend = ObjectUtil.isNotNull(roleControlOpenRo)
            && BooleanUtil.isTrue(roleControlOpenRo.getIncludeExtend());
        iNodeRoleService.enableNodeRole(userId, spaceId, nodeId, includeExtend);
        // publish space audit events
        ClientOriginInfo clientOriginInfo = InformationUtil
            .getClientOriginInfoInCurrentHttpContext(true, false);
        AuditSpaceArg arg =
            AuditSpaceArg.builder().action(AuditSpaceAction.ENABLE_NODE_ROLE).userId(userId)
                .nodeId(nodeId)
                .requestIp(clientOriginInfo.getIp())
                .requestUserAgent(clientOriginInfo.getUserAgent())
                .info(JSONUtil.createObj()
                    .set(AuditConstants.INCLUDE_EXTEND, BooleanUtil.isTrue(includeExtend))).build();
        SpringContextHolder.getApplicationContext().publishEvent(new AuditSpaceEvent(this, arg));
        return ResponseData.success();
    }

    /**
     * Enable role extend.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/enableRoleExtend")
    @Operation(summary = "Enable role extend")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "nodeId", description = "node id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "nodRTGSy43DJ9"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> enableRoleExtend(@RequestParam(name = "nodeId") String nodeId) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(nodeId), PermissionException.NODE_OPERATION_DENIED);
        // check whether user have permission
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node must be specified before it is turned off.
        iControlService.checkControlStatus(nodeId,
            status -> ExceptionUtil.isTrue(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // close the node to specify permissions
        iNodeRoleService.disableNodeRole(userId, nodeId);
        // publish space audit events
        ClientOriginInfo clientOriginInfo = InformationUtil
            .getClientOriginInfoInCurrentHttpContext(true, false);
        AuditSpaceArg arg =
            AuditSpaceArg.builder().action(AuditSpaceAction.DISABLE_NODE_ROLE).userId(userId)
                .nodeId(nodeId)
                .requestIp(clientOriginInfo.getIp())
                .requestUserAgent(clientOriginInfo.getUserAgent())
                .build();
        SpringContextHolder.getApplicationContext().publishEvent(new AuditSpaceEvent(this, arg));
        return ResponseData.success();
    }

    /**
     * Create node role.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/addRole")
    @Operation(summary = "Create node role",
        description = "Add the organizational unit of the node specified role")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> createRole(@RequestBody @Valid AddNodeRoleRo data) {
        Long userId = SessionContext.getUserId();
        // =The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(data.getNodeId()),
            PermissionException.NODE_OPERATION_DENIED);
        // check whether user have permission
        controlTemplate.checkNodePermission(memberId, data.getNodeId(),
            NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node, which must be the specified mode.
        iControlService.checkControlStatus(data.getNodeId(),
            status -> ExceptionUtil.isTrue(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // Check whether the added organizational unit ID has the current space
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        // add node role
        iNodeRoleService.addNodeRole(userId, data.getNodeId(), data.getRole(), data.getUnitIds());
        // publish space audit events
        ClientOriginInfo clientOriginInfo = InformationUtil
            .getClientOriginInfoInCurrentHttpContext(true, false);
        AuditSpaceArg arg =
            AuditSpaceArg.builder().action(AuditSpaceAction.ADD_NODE_ROLE).userId(userId)
                .nodeId(data.getNodeId())
                .requestIp(clientOriginInfo.getIp())
                .requestUserAgent(clientOriginInfo.getUserAgent())
                .info(JSONUtil.createObj().set(AuditConstants.UNIT_IDS, data.getUnitIds())
                    .set(AuditConstants.ROLE, data.getRole())).build();
        SpringContextHolder.getApplicationContext().publishEvent(new AuditSpaceEvent(this, arg));
        return ResponseData.success();
    }

    /**
     * Edit node role.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/editRole")
    @Operation(summary = "Edit node role",
        description = "Modify the role of the organizational unit of the node")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> editRole(@RequestBody @Valid ModifyNodeRoleRo data) {
        BatchModifyNodeRoleRo ro = new BatchModifyNodeRoleRo();
        ro.setNodeId(data.getNodeId());
        ro.setUnitIds(CollUtil.newArrayList(data.getUnitId()));
        ro.setRole(data.getRole());
        return batchEditRole(ro);
    }

    /**
     * Batch edit role.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/batchEditRole")
    @Operation(summary = "Batch edit role",
        description = "Batch modify the role of the organizational unit of the node")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> batchEditRole(@RequestBody @Valid BatchModifyNodeRoleRo data) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(data.getNodeId()),
            PermissionException.NODE_OPERATION_DENIED);
        // check whether user have permission
        controlTemplate.checkNodePermission(memberId, data.getNodeId(),
            NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node, which must be the specified mode.
        iControlService.checkControlStatus(data.getNodeId(),
            status -> ExceptionUtil.isTrue(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // Check whether the added organizational unit ID has the current space
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        // modify role
        iNodeRoleService.updateNodeRole(userId, data.getNodeId(), data.getRole(),
            data.getUnitIds());
        return ResponseData.success();
    }

    /**
     * Delete role.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/deleteRole", method = RequestMethod.DELETE)
    @Operation(summary = "Delete role")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> deleteRole(@RequestBody @Valid DeleteNodeRoleRo data) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(data.getNodeId()),
            PermissionException.NODE_OPERATION_DENIED);
        // check whether you have permission
        controlTemplate.checkNodePermission(memberId, data.getNodeId(),
            NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node, which must be the specified mode.
        iControlService.checkControlStatus(data.getNodeId(),
            status -> ExceptionUtil.isTrue(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // Check whether the added organizational unit ID has the current space
        iUnitService.checkInSpace(spaceId, Collections.singletonList(data.getUnitId()));
        // Deletes the specified organizational unit of the node
        iNodeRoleService.deleteNodeRole(userId, data.getNodeId(), data.getUnitId());
        return ResponseData.success();
    }

    /**
     * Batch delete node role.
     */
    @Notification(templateId = NotificationTemplateId.NODE_UPDATE_ROLE)
    @PostResource(path = "/batchDeleteRole", method = RequestMethod.DELETE)
    @Operation(summary = "Batch delete node role")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = ParamsConstants.PLAYER_SOCKET_ID, description = "user socket id",
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "QkKp9XJEl")
    })
    public ResponseData<Void> batchDeleteRole(@RequestBody @Valid BatchDeleteNodeRoleRo data) {
        Long userId = SessionContext.getUserId();
        // The method includes determining whether a node exists.
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        SpaceHolder.set(spaceId);
        // The method includes determining whether the user is in this space.
        Long memberId = userSpaceCacheService.getMemberId(userId, spaceId);
        // the root node cannot be operated
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        ExceptionUtil.isFalse(rootNodeId.equals(data.getNodeId()),
            PermissionException.NODE_OPERATION_DENIED);
        // check whether you have permission
        controlTemplate.checkNodePermission(memberId, data.getNodeId(),
            NodePermission.ASSIGN_NODE_ROLE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_OPERATION_DENIED));
        // The permission mode of the check node, which must be the specified mode.
        iControlService.checkControlStatus(data.getNodeId(),
            status -> ExceptionUtil.isTrue(status,
                PermissionException.NODE_ROLE_HAS_DISABLE_EXTEND));
        // Check whether the added organizational unit ID has the current space
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        // Deletes the specified organizational unit of the node
        List<ControlRoleInfo> controlRoles =
            iNodeRoleService.deleteNodeRoles(data.getNodeId(), data.getUnitIds());
        // publish space audit events
        JSONObject info = JSONUtil.createObj();
        List<Long> unitIds =
            controlRoles.stream().map(ControlRoleInfo::getUnitId).collect(toList());
        List<String> oldRoles =
            controlRoles.stream().map(ControlRoleInfo::getRole).collect(toList());
        info.set(AuditConstants.UNIT_IDS, unitIds);
        info.set(AuditConstants.OLD_ROLES, oldRoles);
        ClientOriginInfo clientOriginInfo = InformationUtil
            .getClientOriginInfoInCurrentHttpContext(true, false);
        AuditSpaceArg arg =
            AuditSpaceArg.builder().action(AuditSpaceAction.DELETE_NODE_ROLE).userId(userId)
                .requestIp(clientOriginInfo.getIp())
                .requestUserAgent(clientOriginInfo.getUserAgent())
                .nodeId(data.getNodeId()).info(info).build();
        SpringContextHolder.getApplicationContext().publishEvent(new AuditSpaceEvent(this, arg));
        return ResponseData.success();
    }
}
