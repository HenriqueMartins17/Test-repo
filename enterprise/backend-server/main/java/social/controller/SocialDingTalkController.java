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

package com.apitable.enterprise.social.controller;

import static com.apitable.core.constants.ResponseExceptionConstants.DEFAULT_ERROR_CODE;
import static com.apitable.core.constants.ResponseExceptionConstants.DEFAULT_ERROR_MESSAGE;
import static com.apitable.enterprise.social.enums.SocialException.CONTACT_SYNCING;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_DD_CONFIG_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_INTERNAL_GOODS_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_INTERNAL_GOODS_NOT_EXITS;
import static com.apitable.enterprise.social.enums.SocialException.ONLY_TENANT_ADMIN_BOUND_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.SPACE_HAS_BOUND_TENANT;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_APP_BIND_INFO_NOT_EXISTS;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_APP_HAS_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_APP_IS_HIDDEN;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_DISABLED;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_EXIST;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_BIND_FEISHU;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.event.dingtalk.DingTalkCardFactory;
import com.apitable.enterprise.social.model.BaseDingTalkDaVo;
import com.apitable.enterprise.social.model.DingTalkAgentBindSpaceDTO;
import com.apitable.enterprise.social.model.DingTalkBindSpaceVo;
import com.apitable.enterprise.social.model.DingTalkContactDTO;
import com.apitable.enterprise.social.model.DingTalkDaCreateTemplateDTO;
import com.apitable.enterprise.social.model.DingTalkDaTemplateCreateRo;
import com.apitable.enterprise.social.model.DingTalkDaTemplateDeleteRo;
import com.apitable.enterprise.social.model.DingTalkDaTemplateUpdateRo;
import com.apitable.enterprise.social.model.DingTalkDdConfigRo;
import com.apitable.enterprise.social.model.DingTalkDdConfigVo;
import com.apitable.enterprise.social.model.DingTalkInternalSkuPageRo;
import com.apitable.enterprise.social.model.DingTalkIsvAdminUserLoginVo;
import com.apitable.enterprise.social.model.DingTalkIsvAminUserLoginRo;
import com.apitable.enterprise.social.model.DingTalkIsvUserLoginRo;
import com.apitable.enterprise.social.model.DingTalkIsvUserLoginVo;
import com.apitable.enterprise.social.model.DingTalkTenantMainAdminChangeRo;
import com.apitable.enterprise.social.model.DingTalkUserLoginRo;
import com.apitable.enterprise.social.model.DingTalkUserLoginVo;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.model.TenantDetailVO;
import com.apitable.enterprise.social.service.IDingTalkDaService;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.IDingtalkInternalEventService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.service.ISocialUserService;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.space.dto.GetSpaceListFilterCondition;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import com.vikadata.social.dingtalk.DingtalkConfig.AgentApp;
import com.vikadata.social.dingtalk.message.Message;
import com.vikadata.social.dingtalk.model.DingTalkSsoUserInfoResponse;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import com.vikadata.social.dingtalk.model.UserInfoV2;
import com.vikadata.social.dingtalk.util.DdConfigSign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Third party platform integration interface -- DingTalk.
 * </p>
 */
@RestController
@ApiResource(path = "/social")
@Tag(name = "Third party platform integration interface -- DingTalk")
@Slf4j
public class SocialDingTalkController {

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private IDingTalkService dingTalkService;

    @Resource
    private IUserService userService;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Resource
    private IMemberService memberService;

    @Resource
    private ISpaceService spaceService;

    @Resource
    private EventBusFacade eventBusFacade;

    @Resource
    private ISocialTenantUserService tenantUserService;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialService iSocialService;

    @Resource
    private IDingTalkDaService iDingTalkDaService;

    @Resource
    private ISocialUserService socialUserService;

    @Resource
    private IDingtalkInternalEventService iDingtalkInternalEventService;

    /**
     * DingTalk Application user login.
     */
    @PostResource(path = "/dingtalk/agent/{agentId}/user/login", requiredLogin = false)
    @Operation(summary = "DingTalk Application user login", description = "Use DingTalk login "
        + "user identity to authorize login. If no user is bound, return parameters to guide "
        + "registration")
    public ResponseData<DingTalkUserLoginVo> dingTalkUserLogin(
        @PathVariable("agentId") String agentId, @RequestBody @Valid DingTalkUserLoginRo body) {
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        // The third-party application is not configured
        ExceptionUtil.isNotNull(agentApp, TENANT_NOT_EXIST);
        // Check whether the tenant of the user has been activated
        SocialTenantEntity tenant =
            iSocialTenantService.getByAppIdAndTenantId(agentApp.getCustomKey(),
                agentApp.getCorpId());
        UserInfoV2 userInfo = dingTalkService.getUserInfoByCode(agentId, body.getCode());
        // Tenant not bound, not administrator, returned unbound
        ExceptionUtil.isFalse(tenant == null && !userInfo.getSys(), TENANT_NOT_BIND_SPACE);
        // The tenant is deactivated, but not returned by the administrator
        ExceptionUtil.isFalse(tenant != null && !tenant.getStatus() && !userInfo.getSys(),
            TENANT_DISABLED);
        // Get DingTalk user details
        DingTalkUserDetail userDetail =
            dingTalkService.getUserDetailByUserId(agentId, userInfo.getUserid());
        Long userId = iSocialService.createSocialUser(
            new SocialUser(userDetail.getName(), userDetail.getAvatar(),
                agentApp.getCustomKey(), agentApp.getCorpId(), userDetail.getUserid(),
                userDetail.getUnionid(),
                SocialPlatformType.DINGTALK));
        // Return information
        DingTalkUserLoginVo vo = new DingTalkUserLoginVo();
        // Return to space information list
        GetSpaceListFilterCondition condition = new GetSpaceListFilterCondition();
        condition.setManageable(userDetail.getAdmin());
        vo.setSpaces(spaceService.getSpaceListByUserId(userId, condition));
        String bindSpaceId = socialTenantBindService.getTenantBindSpaceId(agentApp.getCorpId(),
            agentApp.getCustomKey());
        // Need to bind space, return the number of people that can be synchronized
        if (StrUtil.isBlank(bindSpaceId)) {
            vo.setActiveMemberCount(dingTalkService.getAppVisibleUserCount(agentId));
        } else {
            // Activate Space
            socialUserService.dingTalkActiveMember(userId, bindSpaceId, userDetail);
        }
        vo.setBindSpaceId(bindSpaceId);
        // Save Session
        SessionContext.setUserId(userId);
        userService.updateLoginTime(userId);
        // Shence Burial Point - Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(userId,
                "DingTalk Password free login", false, origin)));
        return ResponseData.success(vo);
    }

    /**
     * SV Third party Ding Talk application user login.
     */
    @PostResource(path = "/dingtalk/suite/{suiteId}/user/login", requiredLogin = false)
    @Operation(summary = "ISV Third party Ding Talk application user login", description = "Use "
        + "the third-party DingTalk login user identity to authorize login. If no user is bound, "
        + "return the parameter to guide the registration")
    @Parameter(name = "suiteId", description = "kit ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "111108bb8e6dbc2xxxx")
    public ResponseData<DingTalkIsvUserLoginVo> isvUserLogin(
        @PathVariable("suiteId") String suiteId,
        @RequestBody @Valid DingTalkIsvUserLoginRo body) {
        String tenantId = body.getCorpId();
        // Check whether the tenant of the user has been activated
        SocialTenantEntity tenant = iSocialTenantService.getByAppIdAndTenantId(suiteId, tenantId);
        ExceptionUtil.isNotNull(tenant, TENANT_NOT_EXIST);
        ExceptionUtil.isTrue(tenant.getStatus(), TENANT_NOT_EXIST);
        // Get DingTalk user information
        DingTalkUserDetail userDetail =
            iDingTalkInternalService.getUserDetailByCode(suiteId, tenantId, body.getCode());
        ExceptionUtil.isFalse(userDetail == null, USER_NOT_EXIST);
        String bindSpaceId = socialTenantBindService.getTenantBindSpaceId(tenantId, suiteId);
        // There is a problem with the synchronization data. There is no binding space
        ExceptionUtil.isFalse(bindSpaceId == null, TENANT_NOT_BIND_SPACE);
        MemberEntity member =
            memberService.getBySpaceIdAndOpenId(bindSpaceId, userDetail.getUserid());
        // Synchronizing address book
        ExceptionUtil.isFalse(member == null && iSocialService.isContactSyncing(bindSpaceId),
            CONTACT_SYNCING);
        ExceptionUtil.isFalse(member == null, USER_NOT_EXIST);
        boolean shouldRename =
            iSocialUserBindService.getUserIdByUnionId(userDetail.getUnionid()) == null;
        // Create or obtain user ID
        final Long userId = iSocialService.createSocialUser(
            new SocialUser(member.getMemberName(), userDetail.getAvatar(),
                suiteId, tenantId, userDetail.getUserid(), userDetail.getUnionid(),
                SocialPlatformType.DINGTALK));
        // Return information
        DingTalkIsvUserLoginVo vo = new DingTalkIsvUserLoginVo();
        vo.setBindSpaceId(bindSpaceId);
        vo.setShouldRename(shouldRename);
        vo.setDefaultName(member.getMemberName());
        // Save session
        SessionContext.setUserId(userId);
        userService.updateLoginTime(userId);
        // Shence Burial Point - Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        String scene = StrUtil.isNotBlank(body.getBizAppId())
            ? "DingTalkDa ISV Password free login" : "DingTalk ISV Password free login";
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(userId, scene, false, origin)));
        return ResponseData.success(vo);
    }

    /**
     * ISV third-party DingTalk application background administrator login.
     */
    @PostResource(path = "/dingtalk/suite/{suiteId}/admin/login", requiredLogin = false)
    @Operation(summary = "ISV third-party DingTalk application background administrator login",
        description = "DingTalk workbench entry, administrator login")
    @Parameter(name = "suiteId", description = "kit ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "111108bb8e6dbc2xxxx")
    public ResponseData<DingTalkIsvAdminUserLoginVo> isvAminUserLogin(
        @PathVariable("suiteId") String suiteId,
        @RequestBody @Valid DingTalkIsvAminUserLoginRo body) {
        Long userId = SessionContext.getUserIdWithoutException();
        String bindSpaceId;
        String tenantId;
        if (userId != null && StrUtil.isNotBlank(body.getCorpId())) {
            tenantId = body.getCorpId();
            bindSpaceId = iSocialTenantBindService.getTenantBindSpaceId(tenantId, suiteId);
        } else {
            // Get the login user information of Ding Talk workbench
            DingTalkSsoUserInfoResponse userInfo =
                iDingTalkInternalService.getSsoUserInfoByCode(suiteId, body.getCode());
            ExceptionUtil.isFalse(userInfo == null, USER_NOT_EXIST);
            tenantId = userInfo.getCorpInfo().getCorpid();
            // Check whether the tenant of the user has been activated
            SocialTenantEntity tenant =
                iSocialTenantService.getByAppIdAndTenantId(suiteId, tenantId);
            ExceptionUtil.isNotNull(tenant, TENANT_NOT_EXIST);
            ExceptionUtil.isTrue(tenant.getStatus(), TENANT_NOT_EXIST);
            ExceptionUtil.isTrue(userInfo.getIsSys(), ONLY_TENANT_ADMIN_BOUND_ERROR);
            bindSpaceId = socialTenantBindService.getTenantBindSpaceId(tenantId, suiteId);
            // There is a problem with the synchronization data. There is no binding space
            ExceptionUtil.isFalse(bindSpaceId == null, TENANT_NOT_BIND_SPACE);
            String openId = userInfo.getUserInfo().getUserid();
            DingTalkUserDetail userDetail =
                iDingTalkInternalService.getUserDetailByUserId(suiteId, tenantId, openId);
            MemberEntity member = memberService.getBySpaceIdAndOpenId(bindSpaceId, openId);
            ExceptionUtil.isFalse(member == null, USER_NOT_EXIST);
            userId = iSocialService.createSocialUser(
                new SocialUser(member.getMemberName(), userDetail.getAvatar(),
                    suiteId, tenantId, openId, userDetail.getUnionid(),
                    SocialPlatformType.DINGTALK));
            // Save session
            SessionContext.setUserId(userId);
            userService.updateLoginTime(userId);
            // Shence Burial Point - Login
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
            Long finalUserId = userId;
            TaskManager.me().execute(() ->
                eventBusFacade.onEvent(new UserLoginEvent(finalUserId,
                    "DIngTalk ISV Password free login of workbench administrator",
                    false, origin)));
        }
        // Return information
        DingTalkIsvAdminUserLoginVo vo = new DingTalkIsvAdminUserLoginVo();
        vo.setBindSpaceId(bindSpaceId);
        vo.setCorpId(tenantId);
        return ResponseData.success(vo);
    }

    /**
     * DingTalk The application enterprise binds the space.
     */
    @PostResource(path = "/dingtalk/agent/{agentId}/bindSpace", requiredPermission = false)
    @Operation(summary = "DingTalk The application enterprise binds the space", description =
        "DingTalk application bind space")
    public ResponseData<Void> bindSpace(@PathVariable("agentId") String agentId,
                                        @RequestBody @Valid DingTalkAgentBindSpaceDTO body) {
        Long userId = SessionContext.getUserId();
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        // Check whether the space has been bound to other platform tenants
        boolean spaceBindStatus = socialTenantBindService.getSpaceBindStatus(body.getSpaceId());
        ExceptionUtil.isFalse(spaceBindStatus, SPACE_HAS_BOUND_TENANT);
        // Check whether the application has been bound to other space
        boolean appBindStatus =
            socialTenantBindService.getDingTalkTenantBindStatus(agentApp.getCorpId(),
                agentApp.getCustomKey());
        ExceptionUtil.isFalse(appBindStatus, TENANT_APP_HAS_BIND_SPACE);
        LinkedHashMap<Long, DingTalkContactDTO> contact =
            dingTalkService.getContactTreeMap(agentId);
        Set<String> tenantUserIds =
            iDingtalkInternalEventService.dingTalkAppBindSpace(agentId, body.getSpaceId(), userId,
                contact);
        if (!tenantUserIds.isEmpty()) {
            // Send the <<Start Use>> message card to the synchronized user
            Message cardMessage = DingTalkCardFactory.createEntryCardMsg(agentId);
            TaskManager.me().execute(
                () -> dingTalkService.asyncSendCardMessageToUserPrivate(agentId, cardMessage,
                    new ArrayList<>(tenantUserIds)));
        }
        return ResponseData.success();
    }

    /**
     * Get the space station ID bound by the application.
     */
    @GetResource(path = "/dingtalk/agent/{agentId}/bindSpace", requiredPermission = false)
    @Operation(summary = "Get the space station ID bound by the application", description = "Get "
        + "the space station ID of the application binding, and jump to the login page when "
        + "success=false")
    public ResponseData<DingTalkBindSpaceVo> bindSpaceInfo(
        @PathVariable("agentId") String agentId) {
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        // The third-party application is not configured
        ExceptionUtil.isNotNull(agentApp, TENANT_NOT_EXIST);
        Long userId = SessionContext.getUserId();
        String bindSpaceId = socialTenantBindService.getTenantBindSpaceId(agentApp.getCorpId(),
            agentApp.getCustomKey());
        ExceptionUtil.isFalse(StrUtil.isBlank(bindSpaceId), TENANT_NOT_BIND_SPACE);
        // Detect whether the user binds the space
        Long memberId = memberService.getMemberIdByUserIdAndSpaceId(userId, bindSpaceId);
        ExceptionUtil.isFalse(memberId == null, USER_NOT_BIND_FEISHU);
        return ResponseData.success(DingTalkBindSpaceVo.builder().bindSpaceId(bindSpaceId).build());
    }

    /**
     * ISV Third party application obtains the space ID bound by the application.
     */
    @GetResource(path = "/dingtalk/suite/{suiteId}/bindSpace", requiredPermission = false)
    @Operation(summary = "ISV Third party application obtains the space ID bound by the "
        + "application", description = "Get the space station ID of the application binding, and "
        + "jump to the login page when success=false")
    @Parameters({
        @Parameter(name = "suiteId", description = "kit ID", in = ParameterIn.PATH, required = true,
            schema = @Schema(type = "string"), example = "111108bb8e6dbc2xxxx"),
        @Parameter(name = "corpId", description = "Current Organization ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "aaadd")
    })
    public ResponseData<DingTalkBindSpaceVo> isvBindSpaceInfo(
        @PathVariable("suiteId") String suiteId, @RequestParam("corpId") String corpId) {
        ExceptionUtil.isNotNull(corpId, TENANT_NOT_BIND_SPACE);
        Long userId = SessionContext.getUserId();
        ExceptionUtil.isTrue(iSocialTenantService.isTenantActive(corpId, suiteId),
            TENANT_APP_IS_HIDDEN);
        String bindSpaceId = socialTenantBindService.getTenantBindSpaceId(corpId, suiteId);
        ExceptionUtil.isFalse(StrUtil.isBlank(bindSpaceId), TENANT_NOT_BIND_SPACE);
        // Detect whether the user binds the space
        Long memberId = memberService.getMemberIdByUserIdAndSpaceId(userId, bindSpaceId);
        ExceptionUtil.isFalse(memberId == null, USER_NOT_BIND_FEISHU);
        return ResponseData.success(DingTalkBindSpaceVo.builder().bindSpaceId(bindSpaceId).build());
    }

    /**
     * Refresh the address book of DingTalk application.
     */
    @GetResource(path = "/dingtalk/agent/refresh/contact", requiredPermission = false)
    @Operation(summary = "Refresh the address book of DingTalk application", description =
        "Refresh the address book of DingTalk application")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW")
    public ResponseData<Void> refreshContact() {
        String spaceId = LoginContext.me().getSpaceId();
        Long loginMemberId = LoginContext.me().getMemberId();
        TenantBindDTO bindInfo = socialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        ExceptionUtil.isFalse(bindInfo == null, TENANT_NOT_BIND_SPACE);
        String agentId =
            iSocialTenantService.getDingTalkAppAgentId(bindInfo.getTenantId(), bindInfo.getAppId());
        // Primary administrator member ID of the space
        Long mainAdminMemberId = spaceService.getSpaceMainAdminMemberId(spaceId);
        ExceptionUtil.isTrue(mainAdminMemberId.equals(loginMemberId),
            ONLY_TENANT_ADMIN_BOUND_ERROR);
        String openId = memberService.getOpenIdByMemberId(mainAdminMemberId);
        // Compatible with new and old data
        if (StrUtil.isBlank(openId)) {
            openId = tenantUserService.getOpenIdByTenantIdAndUserId(agentId, bindInfo.getTenantId(),
                SessionContext.getUserId());
            if (StrUtil.isBlank(openId)) {
                openId = tenantUserService.getOpenIdByTenantIdAndUserId(bindInfo.getAppId(),
                    bindInfo.getTenantId(),
                    SessionContext.getUserId());
            }
        }
        LinkedHashMap<Long, DingTalkContactDTO> contactMap =
            dingTalkService.getContactTreeMap(agentId);
        Set<String> openIds =
            iDingtalkInternalEventService.dingTalkRefreshContact(spaceId, agentId, openId,
                contactMap);
        if (!openIds.isEmpty()) {
            // Send the <<Start Use>> message card to the synchronized user
            Message cardMessage = DingTalkCardFactory.createEntryCardMsg(agentId);
            TaskManager.me().execute(
                () -> dingTalkService.asyncSendCardMessageToUserPrivate(agentId, cardMessage,
                    new ArrayList<>(openIds)));
        }
        return ResponseData.success();
    }

    /**
     * Tenant space replacement master administrator.
     */
    @PostResource(path = "/dingtalk/suite/{suiteId}/changeAdmin", requiredPermission = false)
    @Operation(summary = "Tenant space replacement master administrator",
        description = "Replace the master administrator")
    @Parameter(name = "suiteId", description = "kit ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "111108bb8e6dbc2xxxx")
    public ResponseData<Void> changeAdmin(@PathVariable("suiteId") String suiteId,
                                          @RequestBody @Valid
                                          DingTalkTenantMainAdminChangeRo opRo) {
        Long userId = SessionContext.getUserId();
        String tenantKey = opRo.getCorpId();
        // Check whether the user is in the tenant and an administrator
        String openId =
            iSocialUserBindService.getOpenIdByTenantIdAndUserId(suiteId, tenantKey, userId);
        ExceptionUtil.isNotNull(openId, USER_NOT_EXIST);
        DingTalkUserDetail userDetail =
            iDingTalkInternalService.getUserDetailByUserId(suiteId, tenantKey, openId);
        ExceptionUtil.isTrue(userDetail.getAdmin(), ONLY_TENANT_ADMIN_BOUND_ERROR);
        // Verify whether the current user is in the tenant
        iSocialService.changeMainAdmin(opRo.getSpaceId(), opRo.getMemberId());
        return ResponseData.success();
    }

    /**
     * Get tenant binding information.
     */
    @GetResource(path = "/dingtalk/suite/{suiteId}/detail", requiredPermission = false)
    @Operation(summary = "Get tenant binding information", description = "Get the space "
        + "information bound by the tenant")
    @Parameters({
        @Parameter(name = "suiteId", description = "kit ID", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.PATH, example = "111108bb8e6dbc2xxxx"),
        @Parameter(name = "corpId", description = "current organization ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "aaadd")
    })
    public ResponseData<TenantDetailVO> getTenantInfo(@PathVariable("suiteId") String suiteId,
                                                      @RequestParam("corpId") String corpId) {
        ExceptionUtil.isNotNull(corpId, TENANT_NOT_BIND_SPACE);
        Long userId = SessionContext.getUserId();
        ExceptionUtil.isTrue(iSocialTenantService.isTenantActive(corpId, suiteId),
            TENANT_APP_IS_HIDDEN);
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(corpId, suiteId);
        ExceptionUtil.isFalse(StrUtil.isBlank(spaceId), TENANT_NOT_BIND_SPACE);
        // Check whether the user is in the tenant and an administrator
        String openId =
            iSocialUserBindService.getOpenIdByTenantIdAndUserId(suiteId, corpId, userId);
        ExceptionUtil.isFalse(StrUtil.isBlank(openId), USER_NOT_EXIST);
        DingTalkUserDetail userDetail =
            iDingTalkInternalService.getUserDetailByUserId(suiteId, corpId, openId);
        ExceptionUtil.isTrue(userDetail.getAdmin(), ONLY_TENANT_ADMIN_BOUND_ERROR);
        return ResponseData.success(iSocialService.getTenantInfo(corpId, suiteId));
    }

    /**
     * Get the SKU page address of domestic products.
     */
    @PostResource(path = "/dingtalk/skuPage", requiredPermission = false)
    @Operation(summary = "Get the SKU page address of domestic products", description = "Get the "
        + "SKU page address of domestic products")
    public ResponseData<String> getSkuPage(@RequestBody @Valid DingTalkInternalSkuPageRo body) {
        TenantBindDTO bindInfo =
            socialTenantBindService.getTenantBindInfoBySpaceId(body.getSpaceId());
        ExceptionUtil.isTrue(bindInfo != null, TENANT_APP_BIND_INFO_NOT_EXISTS);
        String corpId = bindInfo.getTenantId();
        ExceptionUtil.isTrue(iSocialTenantService.isTenantActive(corpId, bindInfo.getAppId()),
            TENANT_APP_IS_HIDDEN);
        try {
            String callbackPage =
                StrUtil.blankToDefault(URLUtil.encodeAll(body.getCallbackPage()), "");
            String page = iDingTalkInternalService.getInternalSkuPage(bindInfo.getAppId(), corpId,
                callbackPage,
                body.getExtendParam());
            ExceptionUtil.isFalse(page == null, DING_TALK_INTERNAL_GOODS_NOT_EXITS);
            return ResponseData.success(page);
        } catch (Exception e) {
            log.error("Failed to get the product page:{}", corpId, e);
        }
        throw new BusinessException(DING_TALK_INTERNAL_GOODS_ERROR);
    }

    /**
     * Get the dd.config parameter.
     */
    @PostResource(path = "/dingtalk/ddconfig", requiredPermission = false)
    @Operation(summary = "Get the dd.config parameter", description = "Get the dd.config parameter")
    public ResponseData<DingTalkDdConfigVo> getDdConfigParam(
        @RequestBody @Valid DingTalkDdConfigRo body) {
        TenantBindDTO bindInfo =
            socialTenantBindService.getTenantBindInfoBySpaceId(body.getSpaceId());
        ExceptionUtil.isTrue(bindInfo != null, TENANT_APP_BIND_INFO_NOT_EXISTS);
        String corpId = bindInfo.getTenantId();
        ExceptionUtil.isTrue(iSocialTenantService.isTenantActive(corpId, bindInfo.getAppId()),
            TENANT_APP_IS_HIDDEN);
        String suiteId = bindInfo.getAppId();
        String agentId = iDingTalkInternalService.getIsvDingTalkAgentId(suiteId, corpId);
        ExceptionUtil.isTrue(StrUtil.isNotBlank(agentId), TENANT_APP_IS_HIDDEN);
        try {
            DingTalkDdConfigVo vo = new DingTalkDdConfigVo();
            vo.setAgentId(agentId);
            vo.setNonceStr(DdConfigSign.getRandomStr(10));
            vo.setTimeStamp(Long.toString(System.currentTimeMillis()));
            vo.setCorpId(corpId);
            String sign =
                iDingTalkInternalService.ddConfigSign(bindInfo.getAppId(), corpId, vo.getNonceStr(),
                    vo.getTimeStamp(), body.getUrl());
            vo.setSignature(sign);
            return ResponseData.success(vo);
        } catch (Exception e) {
            log.error("Failed to generate signature:{}", body.getSpaceId(), e);
        }
        throw new BusinessException(DING_TALK_DD_CONFIG_ERROR);
    }

    /**
     * DingTalk Callback interface--Template Creation.
     */
    @PostResource(path = "/dingtalk/template/{dingTalkDaAppId}/create", requiredPermission =
        false, requiredLogin = false)
    @Operation(summary = "DingTalk Callback interface--Template Creation", description =
        "DingTalk Callback interface--Template Creation")
    public void dingTalkDaTemplateCreate(@PathVariable("dingTalkDaAppId") String dingTalkDaAppId,
                                         @RequestBody @Valid DingTalkDaTemplateCreateRo body,
                                         HttpServletResponse response) {
        try {
            iDingTalkDaService.validateSignature(dingTalkDaAppId, body.getCorpId(),
                body.getTimestamp(),
                body.getSignature());
            DingTalkDaCreateTemplateDTO dto =
                iDingTalkDaService.dingTalkDaTemplateCreate(dingTalkDaAppId,
                    body.getCorpId(), body.getTemplateKey(), body.getOpUserId(), body.getName());
            toSuccessResponseData(response, dto);
        } catch (BusinessException e) {
            toErrorResponseData(response, e);
        } catch (Exception e) {
            log.error("DigTalk Template Creation", e);
            toErrorResponseData(response,
                new BusinessException(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE));
        }
    }

    /**
     * DingTalk Callback interface--Template application modification.
     */
    @PostResource(path = "/dingtalk/template/{dingTalkDaAppId}/update", requiredPermission =
        false, requiredLogin = false)
    @Operation(summary = "DingTalk Callback interface--Template application modification",
        description = "DingTalk Callback interface--Template application modification")
    public void dingTalkDaTemplateUpdate(@PathVariable("dingTalkDaAppId") String dingTalkDaAppId,
                                         @RequestBody @Valid DingTalkDaTemplateUpdateRo body,
                                         HttpServletResponse response) {
        try {
            iDingTalkDaService.validateSignature(dingTalkDaAppId, body.getCorpId(),
                body.getTimestamp(), body.getSignature());
            iDingTalkDaService.dingTalkDaTemplateUpdate(dingTalkDaAppId, body);
            toSuccessResponseData(response, null);
        } catch (BusinessException e) {
            toErrorResponseData(response, e);
        } catch (Exception e) {
            toErrorResponseData(response,
                new BusinessException(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE));
        }
    }

    /**
     * DingTalk Callback interface--Template application deletion.
     */
    @PostResource(path = "/dingtalk/template/{dingTalkDaAppId}/delete", requiredPermission =
        false, requiredLogin =
        false)
    @Operation(summary = "DingTalk Callback interface--Template application deletion",
        description = "DingTalk Callback interface--Template application deletion")
    public void dingTalkDaTemplateDelete(@PathVariable("dingTalkDaAppId") String dingTalkDaAppId,
                                         @RequestBody @Valid DingTalkDaTemplateDeleteRo body,
                                         HttpServletResponse response) {
        try {
            iDingTalkDaService.validateSignature(dingTalkDaAppId, body.getCorpId(),
                body.getTimestamp(),
                body.getSignature());
            iDingTalkDaService.dingTalkDaTemplateStatusUpdate(body.getBizAppId(), 2);
            toSuccessResponseData(response, null);
        } catch (BusinessException e) {
            toErrorResponseData(response, e);
        } catch (Exception e) {
            toErrorResponseData(response,
                new BusinessException(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE));
        }

    }

    /**
     * toErrorResponseData.
     */
    private void toErrorResponseData(HttpServletResponse response, BusinessException error) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        BaseDingTalkDaVo vo = new BaseDingTalkDaVo();
        vo.setErrCode(error.getCode());
        vo.setSuccess(false);
        vo.setErrMsg(error.getMessage());
        try (Writer writer = response.getWriter()) {
            writer.write(JSONUtil.toJsonStr(vo));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * toSuccessResponseData.
     */
    private void toSuccessResponseData(HttpServletResponse response, Object data) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        BaseDingTalkDaVo vo = new BaseDingTalkDaVo();
        vo.setSuccess(true);
        vo.setResult(data);
        try (Writer writer = response.getWriter()) {
            writer.write(JSONUtil.toJsonStr(vo));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
