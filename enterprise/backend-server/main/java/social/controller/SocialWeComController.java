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

import static com.apitable.core.util.HttpContextUtil.X_REAL_HOST;
import static com.apitable.enterprise.social.enums.SocialException.APP_HAS_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.ONLY_TENANT_ADMIN_BOUND_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.SPACE_HAS_BOUND_TENANT;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_APP_BIND_INFO_NOT_EXISTS;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_AUTH;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_BIND_WECOM;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST_WECOM;
import static com.apitable.user.enums.UserException.USER_ALREADY_LINK_SAME_TYPE_ERROR_WECOM;
import static com.apitable.workspace.enums.PermissionException.NODE_NOT_EXIST;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.enterprise.social.event.wecom.WeComCardFactory;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.WeComCreateTempConfigResult;
import com.apitable.enterprise.social.ro.HotsTransformIpRo;
import com.apitable.enterprise.social.ro.WeComAgentBindSpaceRo;
import com.apitable.enterprise.social.ro.WeComCheckConfigRo;
import com.apitable.enterprise.social.ro.WeComUserLoginRo;
import com.apitable.enterprise.social.service.ISocialCpUserBindService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.IWeComService;
import com.apitable.enterprise.social.vo.SocialTenantEnvVo;
import com.apitable.enterprise.social.vo.WeComBindConfigVo;
import com.apitable.enterprise.social.vo.WeComBindSpaceVo;
import com.apitable.enterprise.social.vo.WeComCheckConfigVo;
import com.apitable.enterprise.social.vo.WeComUserLoginVo;
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
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import com.vikadata.social.wecom.WeComConfig;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.model.WeComAuthInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.WxCpAgent;
import me.chanjar.weixin.cp.bean.WxCpUser;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Third party platform integration interface -- WeCom.
 * </p>
 */
@RestController
@ApiResource(path = "/social")
@Tag(name = "Third party platform integration interface -- WeCom")
@Slf4j
public class SocialWeComController {

    @Resource
    private IWeComService iWeComService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialCpUserBindService iSocialCpUserBindService;

    @Resource
    private EventBusFacade eventBusFacade;

    @Resource
    private ISocialService iSocialService;

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    /**
     * WeCom Application user login.
     */
    @PostResource(path = "/wecom/user/login", requiredLogin = false)
    @Operation(summary = "WeCom Application user login", description = "Use WeCom login user "
        + "identity to authorize login, and return parameters to guide registration when no user "
        + "is bound")
    public ResponseData<WeComUserLoginVo> weComUserLogin(
        @RequestBody @Valid WeComUserLoginRo body) {
        WxCpAgent corpAgent = iWeComService.getCorpAgent(body.getCorpId(), body.getAgentId());
        if (corpAgent.getClose() == 1) {
            throw new BusinessException(
                String.format("WeCom application「%s」not enabled", corpAgent.getName()));
        }
        // Check whether the scanning user is in the application authorization visible area
        WxCpUser weComUser =
            iWeComService.getWeComUserByOAuth2Code(body.getCorpId(), body.getAgentId(),
                body.getCode());
        ExceptionUtil.isTrue(
            Objects.nonNull(weComUser) && StrUtil.isNotBlank(weComUser.getUserId()),
            USER_NOT_EXIST);
        log.info("WeCom application「{}」, Member:「{} - {}」Request Login", corpAgent.getName(),
            weComUser.getName(), weComUser.getUserId());
        // Check whether the login application is bound to the space station
        String bindSpaceId = iSocialTenantBindService.getTenantBindSpaceId(body.getCorpId(),
            String.valueOf(body.getAgentId()));
        log.info("WeCom application「{}」, Member:「{} - {}」, Request to land on the space:「{} - {}」",
            corpAgent.getName(), weComUser.getName(), weComUser.getUserId(), corpAgent.getName(),
            bindSpaceId);
        ExceptionUtil.isNotBlank(bindSpaceId, NODE_NOT_EXIST);
        // Check whether the login personnel have vika address book
        MemberEntity member =
            iMemberService.getBySpaceIdAndOpenId(bindSpaceId, weComUser.getUserId());
        ExceptionUtil.isNotNull(member, USER_NOT_EXIST_WECOM);
        // Check whether you have logged in. If not, automatically create an account and set the
        // current user to login
        SocialUser user =
            SocialUser.WECOM().tenantId(body.getCorpId()).appId(String.valueOf(body.getAgentId()))
                .openId(weComUser.getUserId())
                .nickName(weComUser.getName()).avatar(weComUser.getAvatar()).build();
        Long userId = iSocialService.createSocialUser(user);
        ExceptionUtil.isNotNull(userId, USER_NOT_AUTH);
        // Automatic login if bound
        SessionContext.setUserId(userId);
        log.info("WeCom application「{}」, Memver:「{} - {}」Login User「{}」enter space「{}」success",
            corpAgent.getName(), weComUser.getName(), weComUser.getUserId(), userId, bindSpaceId);
        // Return the application binding space id
        WeComUserLoginVo result = new WeComUserLoginVo();
        result.setBindSpaceId(bindSpaceId);
        iUserService.updateLoginTime(userId);
        // Shence Burial Point - Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(userId,
                "WeCom Password free login", false, origin)));
        return ResponseData.success(result);
    }

    /**
     * WeCom Verification - Authorization Application Configuration.
     */
    @PostResource(path = "/wecom/check/config", requiredPermission = false)
    @Operation(summary = "WeCom Verification - Authorization Application Configuration",
        description = "Before binding We Com, verify the third-party application configuration in"
            + " advance. If the code scanning verification is not successful, the configuration "
            + "file is not effective")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW")
    public ResponseData<WeComCheckConfigVo> weComCheckConfig(
        @RequestBody @Valid WeComCheckConfigRo body) {
        Long loginMemberId = LoginContext.me().getMemberId();
        String spaceId = LoginContext.me().getSpaceId();
        // Primary administrator member ID of the space
        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        ExceptionUtil.isTrue(mainAdminMemberId.equals(loginMemberId),
            ONLY_TENANT_ADMIN_BOUND_ERROR);
        // Check whether the space has been bound to other platform tenants
        boolean spaceBindStatus = iSocialTenantBindService.getSpaceBindStatus(spaceId);
        ExceptionUtil.isFalse(spaceBindStatus, SPACE_HAS_BOUND_TENANT);
        // Check whether the application has been bound to other space
        boolean appBindStatus = iSocialTenantBindService.getWeComTenantBindStatus(body.getCorpId(),
            String.valueOf(body.getAgentId()));
        ExceptionUtil.isFalse(appBindStatus, APP_HAS_BIND_SPACE);
        // Check the validity of the application configuration file
        WeComCheckConfigVo result = new WeComCheckConfigVo();
        WeComCreateTempConfigResult createResult =
            iWeComService.createTempAgentAuthConfig(body.getCorpId(), body.getAgentId(),
                body.getAgentSecret(), spaceId, true);
        result.setIsPass(StrUtil.isNotBlank(createResult.getConfigSha()));
        result.setConfigSha(createResult.getConfigSha());
        result.setDomainName(createResult.getDomainName());
        return ResponseData.success(result);
    }

    /**
     * WeCom Verification domain name conversion IP.
     */
    @PostResource(path = "/wecom/hotsTransformIp", requiredPermission = false)
    @Operation(summary = "WeCom Verification domain name conversion IP", description = "Used to "
        + "generate We Com scanning code to log in and verify whether the domain name can be "
        + "accessed")
    public ResponseData<Boolean> hotsTransformIp(@RequestBody @Valid HotsTransformIpRo body,
                                                 HttpServletRequest request) {
        if (weComTemplate == null) {
            throw new BusinessException("WeCom is not enabled");
        }
        boolean result = true;
        WeComConfig config = weComTemplate.getConfig();
        if (config.isAutoCreateDomain()) {
            String ipByHost = NetUtil.getIpByHost(body.getDomain());
            result = Validator.isIpv4(ipByHost);
        }
        return ResponseData.success(result);
    }

    /**
     * WeCom Application binding space.
     */
    @PostResource(path = "/wecom/bind/{configSha}/config", requiredPermission = false)
    @Operation(summary = "WeCom Application binding space", description = "WeCom Application "
        + "binding space")
    public ResponseData<Void> weComBindConfig(@PathVariable("configSha") String configSha,
                                              @RequestBody @Valid WeComAgentBindSpaceRo body) {
        Long userId = SessionContext.getUserId();
        // Check whether the binding information is valid
        WeComAuthInfo agentConfig = iWeComService.getConfigSha(configSha);
        ExceptionUtil.isNotNull(agentConfig, TENANT_APP_BIND_INFO_NOT_EXISTS);
        // Check whether the scanning user is in the application authorization visible area
        WxCpUser weComUser = iWeComService.getWeComUserByOAuth2Code(agentConfig.getCorpId(),
            agentConfig.getAgentId(), body.getCode(), true);
        ExceptionUtil.isTrue(
            Objects.nonNull(weComUser) && StrUtil.isNotBlank(weComUser.getUserId()),
            USER_NOT_EXIST);
        // Check whether the space has been bound to other platform tenants
        boolean spaceBindStatus = iSocialTenantBindService.getSpaceBindStatus(body.getSpaceId());
        ExceptionUtil.isFalse(spaceBindStatus, SPACE_HAS_BOUND_TENANT);
        // Check whether the application has been bound to other space stations
        boolean appBindStatus =
            iSocialTenantBindService.getWeComTenantBindStatus(agentConfig.getCorpId(),
                String.valueOf(agentConfig.getAgentId()));
        ExceptionUtil.isFalse(appBindStatus, APP_HAS_BIND_SPACE);
        // Verify whether the binding member vika user is the same
        // It is mainly used to verify the bound members, and cannot bind other members of the
        // same enterprise
        String linkedWeComUserId =
            iSocialCpUserBindService.getOpenIdByTenantIdAndUserId(agentConfig.getCorpId(), userId);
        if (null != linkedWeComUserId) {
            ExceptionUtil.isTrue(linkedWeComUserId.equals(weComUser.getUserId()),
                USER_ALREADY_LINK_SAME_TYPE_ERROR_WECOM);
        }
        // After passing the check, start synchronizing the address book
        agentConfig.setOperatingBindUserId(userId)
            .setOperatingBindWeComUserId(weComUser.getUserId())
            .setOperatingBindWeComUser(weComUser);
        iWeComService.weComAppBindSpace(agentConfig.getCorpId(), agentConfig.getAgentId(),
            body.getSpaceId(), agentConfig);
        // After successfully synchronizing the address book, start sending the welcome card
        // message 「send to all」
        WxCpMessage welcomeMsg = WeComCardFactory.createWelcomeMsg(agentConfig.getAgentId());
        iWeComService.sendMessageToUserPrivate(agentConfig.getCorpId(), agentConfig.getAgentId(),
            body.getSpaceId(), null, welcomeMsg);
        // create menu
        iWeComService.createFixedMenu(agentConfig.getCorpId(), agentConfig.getAgentId(),
            body.getSpaceId());
        return ResponseData.success();
    }

    /**
     * WeCom App Refresh Address Book.
     */
    @GetResource(path = "/wecom/refresh/contact", requiredPermission = false)
    @Operation(summary = "WeCom App Refresh Address Book", description = "WeCom Apply to refresh "
        + "the address book manually")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "space ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW")
    public ResponseData<Void> weComRefreshContact() {
        Long userId = SessionContext.getUserId();
        String spaceId = LoginContext.me().getSpaceId();
        // Query whether the space station is bound to WeCom application
        WeComBindConfigVo bindConfig = iWeComService.getTenantBindWeComConfig(spaceId);
        Set<String> currentSyncWeComUserIds =
            iWeComService.weComRefreshContact(bindConfig.getCorpId(), bindConfig.getAgentId(),
                spaceId, userId);
        if (CollUtil.isNotEmpty(currentSyncWeComUserIds)) {
            // After successfully synchronizing the address book, start sending the welcome card
            // message
            WxCpMessage welcomeMsg = WeComCardFactory.createWelcomeMsg(bindConfig.getAgentId());
            iWeComService.sendMessageToUserPrivate(bindConfig.getCorpId(), bindConfig.getAgentId(),
                spaceId, new ArrayList<>(currentSyncWeComUserIds), welcomeMsg);
        }
        return ResponseData.success();
    }

    /**
     * Get the bound WeCom application configuration of the space station.
     */
    @GetResource(path = "/wecom/get/config", requiredPermission = false)
    @Operation(summary = "Get the bound WeCom application configuration of the space station",
        description = "Get the bound WeCom application configuration of the space station")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "space ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW")
    public ResponseData<WeComBindConfigVo> getTenantBindWeComConfig() {
        Long loginMemberId = LoginContext.me().getMemberId();
        String spaceId = LoginContext.me().getSpaceId();
        // Primary administrator member ID of the space
        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        ExceptionUtil.isTrue(mainAdminMemberId.equals(loginMemberId),
            ONLY_TENANT_ADMIN_BOUND_ERROR);
        WeComBindConfigVo data = iWeComService.getTenantBindWeComConfig(spaceId);
        return ResponseData.success(data);
    }

    /**
     * Obtain the space ID bound by the self built application of WeCom.
     */
    @GetResource(path = "/wecom/agent/get/bindSpace", requiredLogin = false,
        requiredAccessDomain = true)
    @Operation(summary = "Obtain the space ID bound by the self built application of WeCom",
        description = "Get the space ID bound to the self built application of WeCom, and jump to"
            + " the login page when success=false")
    public ResponseData<WeComBindSpaceVo> bindSpaceInfo(
        @RequestParam(value = "corpId") String corpId,
        @RequestParam(value = "agentId") Integer agentId) {
        WxCpAgent corpAgent = iWeComService.getCorpAgent(corpId, agentId);
        if (corpAgent.getClose() == 1) {
            throw new BusinessException(
                String.format("WeCom application「%s」not enabled", corpAgent.getName()));
        }
        Long userId = SessionContext.getUserId();
        // Check whether the application binds space
        String bindSpaceId =
            iSocialTenantBindService.getTenantBindSpaceId(corpId, String.valueOf(agentId));
        ExceptionUtil.isNotBlank(bindSpaceId, TENANT_NOT_BIND_SPACE);
        // Detect whether the user binds the space
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, bindSpaceId);
        ExceptionUtil.isNotNull(memberId, USER_NOT_BIND_WECOM);
        return ResponseData.success(WeComBindSpaceVo.builder().bindSpaceId(bindSpaceId).build());
    }

    /**
     * Get integrated tenant environment configuration.
     */
    @GetResource(path = "/tenant/env", requiredLogin = false, requiredAccessDomain = true)
    @Operation(summary = "Get integrated tenant environment configuration", description = "Get integrated tenant environment configuration")
    @Parameters({
        @Parameter(name = X_REAL_HOST, description = "Real request address", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spch5n5x2572s.enp.vika.ltd")
    })
    public ResponseData<SocialTenantEnvVo> socialTenantEnv(HttpServletRequest request) {
        String remoteHost = HttpContextUtil.getRemoteHost(request);
        return ResponseData.success(iWeComService.getWeComTenantEnv(remoteHost));
    }

}