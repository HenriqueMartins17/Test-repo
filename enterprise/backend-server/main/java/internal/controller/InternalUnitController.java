package com.apitable.enterprise.internal.controller;

import static com.apitable.organization.enums.OrganizationException.DELETE_ROOT_ERROR;
import static com.apitable.organization.enums.OrganizationException.DELETE_SPACE_ADMIN_ERROR;
import static com.apitable.organization.enums.OrganizationException.DUPLICATION_ROLE_NAME;
import static com.apitable.organization.enums.OrganizationException.GET_PARENT_TEAM_ERROR;
import static com.apitable.organization.enums.OrganizationException.GET_TEAM_ERROR;
import static com.apitable.organization.enums.OrganizationException.ILLEGAL_MEMBER_PERMISSION;
import static com.apitable.organization.enums.OrganizationException.ILLEGAL_ROLE_PERMISSION;
import static com.apitable.organization.enums.OrganizationException.ILLEGAL_TEAM_PERMISSION;
import static com.apitable.organization.enums.OrganizationException.NOT_EXIST_MEMBER;
import static com.apitable.organization.enums.OrganizationException.ROLE_EXIST_ROLE_MEMBER;
import static com.apitable.organization.enums.OrganizationException.TEAM_HAS_MEMBER;
import static com.apitable.organization.enums.OrganizationException.TEAM_HAS_SUB;
import static com.apitable.organization.enums.OrganizationException.UPDATE_TEAM_LEVEL_ERROR;
import static com.apitable.shared.constants.NotificationConstants.INVOLVE_MEMBER_ID;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Dict;
import com.apitable.control.infrastructure.permission.space.resource.ResourceCode;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.internal.ro.CreateUnitTeamRo;
import com.apitable.enterprise.internal.ro.UpdateUnitMemberRo;
import com.apitable.enterprise.internal.ro.UpdateUnitRoleRo;
import com.apitable.enterprise.internal.ro.UpdateUnitTeamRo;
import com.apitable.enterprise.internal.service.IInternalUnitService;
import com.apitable.interfaces.social.facade.SocialServiceFacade;
import com.apitable.organization.dto.RoleBaseInfoDto;
import com.apitable.organization.dto.UnitRoleInfoDTO;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.mapper.TeamMapper;
import com.apitable.organization.ro.CreateRoleRo;
import com.apitable.organization.ro.UpdateMemberRo;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.IRoleMemberService;
import com.apitable.organization.service.IRoleService;
import com.apitable.organization.service.ITeamService;
import com.apitable.organization.service.IUnitService;
import com.apitable.organization.vo.UnitMemberInfoVo;
import com.apitable.organization.vo.UnitRoleInfoVo;
import com.apitable.organization.vo.UnitRoleMemberVo;
import com.apitable.organization.vo.UnitTeamInfoVo;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.notification.NotificationManager;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.holder.UserHolder;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.apitable.space.enums.SpaceUpdateOperate;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.service.ISpaceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal org controller.
 */
@RestController
@Tag(name = "Internal Contacts Api")
@ApiResource(path = "/internal/org")
public class InternalUnitController {
    @Resource
    private ITeamService iTeamService;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private SocialServiceFacade socialServiceFacade;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IRoleService iRoleService;

    @Resource
    private IRoleMemberService iRoleMemberService;

    @Resource
    private IInternalUnitService iInternalUnitService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private SpaceMapper spaceMapper;

    /**
     * Query team information.
     */
    @GetResource(path = "/teams/{unitId}/children", name = "Querying team information", requiredPermission = false)
    @Operation(summary = "Query team information", description = "Query department information. if team id lack, default root team")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
        @Parameter(name = PAGE_PARAM, description = "page's parameter", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    public ResponseData<PageInfo<UnitTeamInfoVo>> getTeamChildrenPageList(
        @PathVariable(name = "unitId") String unitId,
        @PageObjectParam Page<Long> page) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.READ_TEAM,
            ILLEGAL_TEAM_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        Long parentTeamId = iTeamService.getTeamIdByUnitId(spaceId, unitId,
            status -> ExceptionUtil.isTrue(status, GET_TEAM_ERROR));
        PageInfo<UnitTeamInfoVo> teams =
            iUnitService.getUnitSubTeamsWithPage(spaceId, parentTeamId, page);
        return ResponseData.success(teams);
    }

    /**
     * Query team information.
     */
    @GetResource(path = "/teams/{unitId}/members", name = "Querying team information", requiredPermission = false)
    @Operation(summary = "Query team members information", description = "Query department members information. if team id lack, default root team")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
        @Parameter(name = "sensitiveData", description = "includes mobile number and email", schema = @Schema(type = "boolean"), in = ParameterIn.QUERY, example = "false"),
        @Parameter(name = PAGE_PARAM, description = "page's parameter", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    public ResponseData<PageInfo<UnitMemberInfoVo>> getTeamMembersPageInfo(
        @PathVariable(name = "unitId") String unitId,
        @RequestParam(name = "sensitiveData", defaultValue = "false") Boolean sensitiveData,
        @PageObjectParam Page<Long> page) {
        String spaceId = LoginContext.me().getSpaceId();
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.READ_MEMBER,
            ILLEGAL_MEMBER_PERMISSION);
        Long parentTeamId = iTeamService.getTeamIdByUnitId(spaceId, unitId,
            status -> ExceptionUtil.isTrue(status, GET_TEAM_ERROR));
        PageInfo<UnitMemberInfoVo> members =
            iUnitService.getMembersByTeamId(spaceId, parentTeamId, sensitiveData, page);
        return ResponseData.success(members);
    }

    /**
     * Create team.
     */
    @PostResource(path = "/teams/create", name = "Create team", requiredPermission = false)
    @Operation(summary = "Add a sub team", description = "Add a sub team")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl")
    public ResponseData<UnitTeamInfoVo> createUnitTeam(@RequestBody @Valid CreateUnitTeamRo data) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.CREATE_TEAM,
            ILLEGAL_TEAM_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        socialServiceFacade.checkCanOperateSpaceUpdate(spaceId, SpaceUpdateOperate.ADD_TEAM);
        Long parentId = iTeamService.getTeamIdByUnitId(spaceId, data.getParentIdUnitId(),
            status -> ExceptionUtil.isTrue(status, GET_PARENT_TEAM_ERROR));
        // check name
        iTeamService.checkNameExists(spaceId, parentId, data.getTeamName());
        // check roles
        if (null != data.getRoleUnitIds()) {
            iInternalUnitService.checkResourcePermission(ResourceCode.ADD_ROLE_MEMBER,
                ILLEGAL_ROLE_PERMISSION);
        }
        List<Long> roleIds = iRoleService.getRoleIdsByUnitIds(spaceId, data.getRoleUnitIds());
        Long teamId = iTeamService.createSubTeam(spaceId, data.getTeamName(), parentId,
            data.getSequence(), roleIds);
        return ResponseData.success(
            iUnitService.getUnitTeamByTeamIds(Collections.singletonList(teamId)).get(0));
    }

    /**
     * Update team info.
     */
    @PostResource(path = "/teams/update/{unitId}", name = "Update team info", requiredPermission = false)
    @Operation(summary = "Update team info", description = "Update team info. If modify team level, default sort in the end of parent team.")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
    })
    public ResponseData<UnitTeamInfoVo> updateUnitTeam(@PathVariable(name = "unitId") String unitId,
                                                       @RequestBody @Valid UpdateUnitTeamRo data) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.UPDATE_TEAM,
            ILLEGAL_TEAM_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        // check team
        TeamEntity department = iTeamService.getTeamByUnitId(spaceId, unitId);
        // check third permission
        socialServiceFacade.checkCanOperateSpaceUpdate(spaceId, SpaceUpdateOperate.UPDATE_TEAM);
        Long parentId = iTeamService.getTeamIdByUnitId(spaceId, data.getParentIdUnitId(),
            status -> ExceptionUtil.isTrue(status, GET_PARENT_TEAM_ERROR));
        // check name
        if (null != data.getTeamName()) {
            iTeamService.checkNameExists(spaceId, parentId, department.getId(), data.getTeamName());
            department.setTeamName(data.getTeamName());
        }
        // check parentId
        if (!department.getParentId().equals(parentId)) {
            List<Long> subIds = iTeamService.getAllTeamIdsInTeamTree(department.getId());
            // The parent department cannot be adjusted to its own child department,
            // nor can it be adjusted below itself, to prevent an infinite loop.
            ExceptionUtil.isFalse(subIds.contains(parentId), UPDATE_TEAM_LEVEL_ERROR);
            department.setParentId(parentId);
            department.setSequence(iTeamService.getMaxSequenceByParentId(parentId) + 1);
        }
        if (null != data.getSequence()) {
            department.setSequence(data.getSequence());
        }
        // check roles
        if (null != data.getRoleUnitIds()) {
            iInternalUnitService.checkResourcePermission(ResourceCode.ADD_ROLE_MEMBER,
                ILLEGAL_ROLE_PERMISSION);
        }
        List<Long> roleIds = iRoleService.getRoleIdsByUnitIds(spaceId, data.getRoleUnitIds());
        iTeamService.updateTeam(department, roleIds);
        return ResponseData.success(
            iUnitService.getUnitTeamByTeamIds(Collections.singletonList(department.getId()))
                .get(0));
    }

    /**
     * Delete team.
     */
    @PostResource(path = "/teams/delete/{unitId}", method = {
        RequestMethod.DELETE}, name = "Delete team")
    @Operation(summary = "Delete team", description = "Delete team. If team has members, it can be deleted.")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
    })
    public ResponseData<Void> deleteUnitTeam(@PathVariable("unitId") String unitId) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.DELETE_TEAM,
            ILLEGAL_TEAM_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        socialServiceFacade.checkCanOperateSpaceUpdate(spaceId, SpaceUpdateOperate.DELETE_TEAM);
        // the root department cannot be deleted
        ExceptionUtil.isFalse(Objects.equals(unitId, "0"), DELETE_ROOT_ERROR);
        // check team
        TeamEntity department = iTeamService.getTeamByUnitId(spaceId, unitId);
        // Query whether there are sub departments under the department
        int retCount = SqlTool.retCount(teamMapper.existChildrenByParentId(department.getId()));
        ExceptionUtil.isTrue(retCount == 0, TEAM_HAS_SUB);
        // query the all team's member number.
        long count = iTeamService.countMemberCountByParentId(department.getId());
        ExceptionUtil.isFalse(count > 0, TEAM_HAS_MEMBER);
        // delete the team
        iTeamService.deleteTeam(department.getId());
        return ResponseData.success();
    }

    /**
     * Query team information.
     */
    @GetResource(path = "/roles", name = "Querying role information")
    @Operation(summary = "Query roles information", description = "Query roles information")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = PAGE_PARAM, description = "page's parameter", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    public ResponseData<PageInfo<UnitRoleInfoVo>> getRolePageList(
        @PageObjectParam Page<RoleBaseInfoDto> page) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.READ_ROLE,
            ILLEGAL_ROLE_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        PageInfo<UnitRoleInfoVo> teams =
            iUnitService.getUnitRolesWithPage(spaceId, page);
        return ResponseData.success(teams);
    }

    /**
     * Query role members.
     */
    @GetResource(path = "/roles/{unitId}/members", name = "query role's members")
    @Operation(summary = "query role members", description = "query the role's members")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
        @Parameter(name = "sensitiveData", description = "includes mobile number and email", schema = @Schema(type = "boolean"), in = ParameterIn.QUERY, example = "false"),
    })
    public ResponseData<UnitRoleMemberVo> getUnitRoleMembers(@PathVariable("unitId") String unitId,
                                                             @RequestParam(name = "sensitiveData", defaultValue = "false")
                                                             Boolean sensitiveData) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.READ_ROLE,
            ILLEGAL_ROLE_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        // check if space has the role.
        Long roleId = iRoleService.getRoleIdByUnitId(spaceId, unitId);
        return ResponseData.success(iUnitService.getRoleMembers(spaceId, roleId, sensitiveData));
    }

    /**
     * Create new role.
     */
    @PostResource(path = "/roles", name = "create role")
    @Operation(summary = "create new role", description = "create new role")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl")
    })
    public ResponseData<UnitRoleInfoVo> createUnitRole(@RequestBody @Valid CreateRoleRo data) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.CREATE_ROLE,
            ILLEGAL_ROLE_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        // check if exist the same role name.
        iRoleService.checkDuplicationRoleName(spaceId, data.getRoleName(),
            status -> ExceptionUtil.isFalse(status, DUPLICATION_ROLE_NAME));
        Long userId = UserHolder.get();
        // add the role.
        UnitRoleInfoDTO role =
            iRoleService.createRole(userId, spaceId, data.getRoleName(), data.getPosition());
        UnitRoleInfoVo vo =
            UnitRoleInfoVo.builder().unitId(role.getUnitId()).name(role.getRoleName())
                .sequence(role.getPosition()).build();
        return ResponseData.success(vo);
    }

    /**
     * Update role information.
     */
    @PostResource(path = "/roles/{unitId}", name = "Update role info")
    @Operation(summary = "Update team info", description = "Update role info.")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
    })
    public ResponseData<UnitRoleInfoVo> updateUnitRole(@PathVariable("unitId") String unitId,
                                                       @RequestBody UpdateUnitRoleRo data) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.UPDATE_ROLE,
            ILLEGAL_ROLE_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        // check if space has the role.
        Long roleId = iRoleService.getRoleIdByUnitId(spaceId, unitId);
        if (data.getRoleName() != null) {
            // check if exist the same role name.
            iRoleService.checkDuplicationRoleName(spaceId, roleId, data.getRoleName(),
                status -> ExceptionUtil.isFalse(status, DUPLICATION_ROLE_NAME));
        }
        Long userId = UserHolder.get();
        // update role information.
        iRoleService.updateRole(userId, roleId, data.getRoleName(), data.getPosition());
        return ResponseData.success(
            iUnitService.getUnitRoleByRoleIds(Collections.singletonList(roleId)).get(0));
    }

    /**
     * Delete team.
     */
    @PostResource(path = "/roles/{unitId}", method = {
        RequestMethod.DELETE}, name = "Delete role")
    @Operation(summary = "Delete team", description = "Delete role. If role has members, it can be deleted.")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
    })
    public ResponseData<Void> deleteUnitRole(@PathVariable("unitId") String unitId) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.DELETE_ROLE,
            ILLEGAL_ROLE_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        // check if space has the role.
        Long roleId = iRoleService.getRoleIdByUnitId(spaceId, unitId);
        // check if role has role members.
        iRoleMemberService.checkRoleMemberExistByRoleId(roleId,
            status -> ExceptionUtil.isFalse(status, ROLE_EXIST_ROLE_MEMBER));
        // delete the role by role id.
        iRoleService.deleteRole(roleId);
        return ResponseData.success();
    }

    /**
     * Query member information.
     */
    @GetResource(path = "/members/{unitId}", name = "Querying member information", requiredPermission = false)
    @Operation(summary = "Query team information", description = "Query team information")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
        @Parameter(name = "sensitiveData", description = "includes mobile number and email", schema = @Schema(type = "boolean"), in = ParameterIn.QUERY, example = "false"),
    })
    public ResponseData<UnitMemberInfoVo> getUnitMemberDetails(
        @PathVariable(name = "unitId") String unitId,
        @RequestParam(name = "sensitiveData", defaultValue = "false") Boolean sensitiveData) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.READ_MEMBER,
            ILLEGAL_MEMBER_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        Long memberId = iMemberService.getMemberIdByUnitId(spaceId, unitId);
        List<UnitMemberInfoVo> members =
            iUnitService.getUnitMemberByMemberIds(Collections.singletonList(memberId),
                sensitiveData);
        ExceptionUtil.isFalse(members.isEmpty(), NOT_EXIST_MEMBER);
        return ResponseData.success(members.get(0));
    }

    /**
     * Edit member info.
     */
    @PostResource(path = "/members/{unitId}", requiredPermission = false)
    @Operation(summary = "Edit member info", description = "Edit member info")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
        @Parameter(name = "sensitiveData", description = "includes mobile number and email", schema = @Schema(type = "boolean"), in = ParameterIn.QUERY, example = "false"),
    })
    public ResponseData<UnitMemberInfoVo> updateUnitMember(
        @PathVariable(name = "unitId") String unitId,
        @RequestParam(name = "sensitiveData", defaultValue = "false") Boolean sensitiveData,
        @RequestBody @Valid UpdateUnitMemberRo data) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.UPDATE_MEMBER,
            ILLEGAL_MEMBER_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        socialServiceFacade.checkCanOperateSpaceUpdate(spaceId, SpaceUpdateOperate.UPDATE_MEMBER);
        Long memberId = iMemberService.getMemberIdByUnitId(spaceId, unitId);
        UpdateMemberRo updateMember = new UpdateMemberRo();
        updateMember.setMemberId(memberId);
        updateMember.setMemberName(data.getMemberName());
        // check roles
        if (null != data.getRoleUnitIds()) {
            iInternalUnitService.checkResourcePermission(ResourceCode.ADD_ROLE_MEMBER,
                ILLEGAL_ROLE_PERMISSION);
            List<Long> roleIds = iRoleService.getRoleIdsByUnitIds(spaceId, data.getRoleUnitIds());
            updateMember.setRoleIds(roleIds);
        }
        // check team
        if (null != data.getTeamUnitIds()) {
            List<Long> teamIds = iTeamService.getTeamIdsByUnitIds(spaceId, data.getTeamUnitIds());
            updateMember.setTeamIds(teamIds);
        }
        iMemberService.updateMember(SessionContext.getUserId(), updateMember);
        List<UnitMemberInfoVo> members =
            iUnitService.getUnitMemberByMemberIds(Collections.singletonList(memberId),
                sensitiveData);
        return ResponseData.success(members.get(0));
    }


    /**
     * Delete a Member.
     */
    @PostResource(path = "/members/{unitId}", method = {
        RequestMethod.DELETE}, requiredPermission = false)
    @Operation(summary = "Delete a Member from organization", description = "Delete a Member from organization")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
        @Parameter(name = "unitId", description = "unit uuid", schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "abcdefg"),
    })
    public ResponseData<Void> deleteMember(@PathVariable("unitId") String unitId) {
        // check resource permission
        iInternalUnitService.checkResourcePermission(ResourceCode.DELETE_MEMBER,
            ILLEGAL_MEMBER_PERMISSION);
        String spaceId = LoginContext.me().getSpaceId();
        Long memberId = iMemberService.getMemberIdByUnitId(spaceId, unitId);
        iSpaceService.checkCanOperateSpaceUpdate(spaceId);
        // delete from space
        Long administrator = spaceMapper.selectSpaceMainAdmin(spaceId);
        // an administrator cannot be deleted
        ExceptionUtil.isFalse(memberId.equals(administrator), DELETE_SPACE_ADMIN_ERROR);
        iMemberService.batchDeleteMemberFromSpace(spaceId,
            Collections.singletonList(memberId), true);
        // notice self
        Long userId = SessionContext.getUserId();
        TaskManager.me().execute(() -> NotificationManager.me()
            .playerNotify(NotificationTemplateId.REMOVE_FROM_SPACE_TO_ADMIN, null, userId,
                spaceId, Dict.create().set(INVOLVE_MEMBER_ID, ListUtil.toList(memberId))));
        TaskManager.me().execute(() -> NotificationManager.me()
            .playerNotify(NotificationTemplateId.REMOVE_FROM_SPACE_TO_USER,
                ListUtil.toList(memberId), userId, spaceId, null));
        TaskManager.me().execute(() -> NotificationManager.me()
            .playerNotify(NotificationTemplateId.REMOVED_MEMBER_TO_MYSELF,
                ListUtil.toList(userId), 0L, spaceId,
                Dict.create().set(INVOLVE_MEMBER_ID, ListUtil.toList(memberId))));
        return ResponseData.success();
    }
}
