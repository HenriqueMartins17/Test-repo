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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.FeishuTenantDetailVO;
import com.apitable.enterprise.social.model.FeishuTenantMainAdminChangeRo;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.properties.FeishuAppProperties;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.context.SessionContext;
import com.apitable.user.service.IUserService;
import com.vikadata.social.feishu.model.FeishuAccessToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Lark access interface.
 */
@RestController
@ApiResource
@Tag(name = "Lark Interface")
@Slf4j
public class FeishuController {

    @Resource
    private ISocialService iSocialService;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private FeishuAppProperties feishuAppProperties;

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ServerProperties serverProperties;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    /**
     * vika integration Lark Portal login callback.
     */
    @GetResource(path = "/social/feishu/login/callback", requiredLogin = false)
    @Operation(summary = "vika integration Lark Portal login callback", description = "The "
        + "exclusive intranet version of the login free scenario will not be available online",
        hidden = true)
    public RedirectView loginCallback(@RequestParam("code") String code) {
        iFeishuService.switchDefaultContext();
        // User's of self built applications will call back the CODE here to ensure that there is
        // only one call after logging in,
        FeishuAccessToken accessToken = iFeishuService.getUserAccessToken(code);
        // Query whether the user's mobile phone number exists. Note that the returned mobile
        // phone number carries the area code
        String mobile = StrUtil.subSuf(accessToken.getMobile(), 3);
        Long userId = iUserService.getUserIdByMobile(mobile);
        if (userId == null) {
            return new RedirectView(constProperties.getServerDomain() + "/workbench");
        }
        SessionContext.setUserId(userId);
        return new RedirectView(constProperties.getServerDomain() + "/workbench");
    }

    /**
     * Application management background authorization callback.
     */
    @GetResource(path = "/social/feishu/admin/callback", requiredLogin = false)
    @Operation(summary = "Application management background authorization callback", description
        = "Store app can only receive callback from Lark app", hidden = true)
    public RedirectView adminCallback(@RequestParam("code") String code) {
        try {
            iFeishuService.switchDefaultContext();
            // It can only be used within the scope of application available authorization
            FeishuAccessToken accessToken = iFeishuService.getUserAccessToken(code);
            boolean isTenantAdmin = iFeishuService.checkUserIsAdmin(accessToken.getTenantKey(),
                accessToken.getOpenId());
            if (!isTenantAdmin) {
                // Non application administrator, redirect to the error page
                return new RedirectView(getErrorPath("is_not_admin"));
            }
            // Create if no user exists
            Long userId = iSocialService.createSocialUser(
                new SocialUser(accessToken.getName(), accessToken.getAvatarUrl(),
                    null, null, null, iFeishuService.getIsvAppId(),
                    accessToken.getTenantKey(), accessToken.getOpenId(), accessToken.getUnionId(),
                    SocialPlatformType.FEISHU));
            SessionContext.setUserId(userId);
            // Redirect to the management page, encrypt the tenant information, and tell the page
            // which tenant is the management page
            String redirectUri =
                constProperties.getServerDomain() + feishuAppProperties.getAdminUri();
            return new RedirectView(StrUtil.format(redirectUri, accessToken.getTenantKey()));
        } catch (Exception exception) {
            log.error("Lark tenant management portal authorization failed", exception);
            return new RedirectView(getErrorPath("auth_fail"));
        }
    }

    /**
     * Enter the application management background authorization callback.
     */
    @GetResource(path = "/social/feishu/admin", requiredLogin = false)
    @Operation(summary = "Enter the application management background authorization callback",
        description = "Only callback from Lark application can be received", hidden = true)
    public RedirectView adminManagePage() {
        // Only construct Lark authorization callback
        iFeishuService.switchDefaultContext();
        String redirectUri =
            constProperties.getServerDomain() + serverProperties.getServlet().getContextPath()
                + "/social/feishu/admin/callback";
        String authUrl =
            iFeishuService.buildAuthUrl(redirectUri, String.valueOf(DateUtil.date().getTime()));
        return new RedirectView(authUrl);
    }

    /**
     * Lark starts using the entrance.
     */
    @GetResource(path = "/social/feishu/entry/callback", requiredLogin = false)
    @Operation(summary = "Lark starts using the entrance", description = "Only callback from Lark"
        + " application can be received", hidden = true)
    public RedirectView feishuEntryCallback(@RequestParam(name = "code") String code,
                                            @RequestParam(name = "url", required = false)
                                            String url,
                                            HttpServletRequest request) {
        log.info("Lark callback received: {}, parameter：{}", request.getRequestURI(),
            request.getQueryString());
        try {
            iFeishuService.switchDefaultContext();
            FeishuAccessToken accessToken = iFeishuService.getUserAccessToken(code);
            // Check whether you have logged in. If not, automatically create an account and set
            // the current user to log in
            Long userId = iSocialService.createSocialUser(
                new SocialUser(accessToken.getName(), accessToken.getAvatarUrl(),
                    null, null, null, iFeishuService.getIsvAppId(),
                    accessToken.getTenantKey(), accessToken.getOpenId(), accessToken.getUnionId(),
                    SocialPlatformType.FEISHU));
            SessionContext.setUserId(userId);
            if (StrUtil.isNotBlank(url)) {
                // Mention notification entry
                return new RedirectView(url);
            } else {
                List<String> spaceIds = iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(
                    accessToken.getTenantKey(), iFeishuService.getIsvAppId());
                String openId = iSocialTenantUserService.getOpenIdByTenantIdAndUserId(
                    iFeishuService.getIsvAppId(), accessToken.getTenantKey(), userId);
                if (StrUtil.isBlank(openId)) {
                    // Jump to the default workbench
                    return new RedirectView(constProperties.getServerDomain() + "/workbench");
                } else {
                    if (CollUtil.isEmpty(spaceIds)) {
                        return new RedirectView(constProperties.getServerDomain() + "/workbench");
                    } else {
                        // Query whether all members of the space have been synchronized
                        String spaceId = CollUtil.getFirst(spaceIds);
                        long memberCount = iMemberService.getTotalMemberCountBySpaceId(spaceId);
                        if (memberCount > 0) {
                            // Jump to the designated space station
                            return new RedirectView(StrUtil.format(
                                constProperties.getServerDomain() + "/space/{}/workbench",
                                spaceId));
                        } else {
                            return new RedirectView(
                                constProperties.getServerDomain() + "/user/social/syncing");
                        }
                    }
                }
            }
        } catch (Exception exception) {
            log.error("Lark application portal authorization failed", exception);
            return new RedirectView(getErrorPath("auth_fail"));
        }
    }

    /**
     * Application portal.
     */
    @GetResource(path = "/social/feishu/entry", requiredLogin = false)
    @Operation(summary = "Application portal", description = "Can only receive clicks from Lark "
        + "app", hidden = true)
    public RedirectView feishuEntry(@RequestParam(name = "url", required = false) String url) {
        // Construct Lark authorization login
        String redirectUri =
            constProperties.getServerDomain() + serverProperties.getServlet().getContextPath()
                + "/social/feishu/entry/callback";
        if (StrUtil.isNotBlank(url)) {
            redirectUri = StrUtil.format(redirectUri + "?url={}", url);
        }
        iFeishuService.switchDefaultContext();
        String authUrl =
            iFeishuService.buildAuthUrl(redirectUri, String.valueOf(DateUtil.date().getTime()));
        return new RedirectView(authUrl);
    }

    /**
     * Get tenant binding information.
     */
    @GetResource(path = "/social/feishu/tenant/{tenantKey}", requiredPermission = false)
    @Parameters({
        @Parameter(name = "tenantKey", description = "Lark Tenant ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "18823789")
    })
    @Operation(summary = "Get tenant binding information", description = "Get the space "
        + "information bound by the tenant")
    public ResponseData<FeishuTenantDetailVO> getTenantInfo(
        @PathVariable("tenantKey") String tenantKey) {
        Long userId = SessionContext.getUserId();
        iFeishuService.switchDefaultContext();
        // Verify whether the current user is in the tenant
        iSocialService.checkUserIfInTenant(userId, iFeishuService.getIsvAppId(), tenantKey);
        return ResponseData.success(
            iSocialService.getFeishuTenantInfo(iFeishuService.getIsvAppId(), tenantKey));
    }

    /**
     * Tenant space replacement master administrator.
     */
    @PostResource(path = "/social/feishu/changeAdmin", requiredPermission = false)
    @Operation(summary = "Tenant space replacement master administrator", description = "Replace "
        + "the master administrator")
    public ResponseData<Void> changeAdmin(@RequestBody @Valid FeishuTenantMainAdminChangeRo opRo) {
        Long userId = SessionContext.getUserId();
        String tenantKey = opRo.getTenantKey();
        // Verify whether the current user is in the tenant
        iFeishuService.switchDefaultContext();
        iSocialService.checkUserIfInTenant(userId, iFeishuService.getIsvAppId(), tenantKey);
        iSocialService.changeMainAdmin(opRo.getSpaceId(), opRo.getMemberId());
        return ResponseData.success();
    }

    private String getErrorPath(String errorKey) {
        String uri = constProperties.getServerDomain() + feishuAppProperties.getErrorUri();
        return StrUtil.format(uri, errorKey);
    }
}
