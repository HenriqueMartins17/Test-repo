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

import static com.apitable.enterprise.social.enums.SocialException.AGENT_CONFIG_DISABLE;
import static com.apitable.enterprise.social.enums.SocialException.ONLY_TENANT_ADMIN_BOUND_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_EXIST;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_AUTH;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;
import static com.apitable.organization.enums.OrganizationException.GET_TEAM_ERROR;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.enterprise.social.model.OneAccessBaseRo;
import com.apitable.enterprise.social.model.OneAccessBaseVo;
import com.apitable.enterprise.social.model.OneAccessCopyInfoRo;
import com.apitable.enterprise.social.model.OneAccessCreateVo;
import com.apitable.enterprise.social.model.OneAccessOrgCreateRo;
import com.apitable.enterprise.social.model.OneAccessOrgDeleteRo;
import com.apitable.enterprise.social.model.OneAccessOrgQueryRo;
import com.apitable.enterprise.social.model.OneAccessOrgUpdateRo;
import com.apitable.enterprise.social.model.OneAccessOrgVo;
import com.apitable.enterprise.social.model.OneAccessUserCreateRo;
import com.apitable.enterprise.social.model.OneAccessUserDeleteRo;
import com.apitable.enterprise.social.model.OneAccessUserQueryRo;
import com.apitable.enterprise.social.model.OneAccessUserSchemaVo;
import com.apitable.enterprise.social.model.OneAccessUserSchemaVo.AttributeEntity;
import com.apitable.enterprise.social.model.OneAccessUserUpdateRo;
import com.apitable.enterprise.social.model.OneAccessUserQueryVo;
import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.enterprise.social.service.IOneAccessService;
import com.apitable.enterprise.social.service.IOneAccessWeComService;
import com.apitable.enterprise.social.util.AESCipher;
import com.apitable.shared.cache.bean.UserSpaceDto;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.constants.SessionAttrConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.RandomExtendUtil;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Third-party platform integration interface--Huawei OneAccess.
 */
@RestController
@ApiResource(path = "/social")
@Tag(name = "Third-party platform integration interface--Huawei OneAccess")
@Slf4j
public class OneAccessController {

    @Resource
    private OneAccessProperties oneAccessProperties;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private IOneAccessService oneAccessService;

    @Resource
    private IOneAccessWeComService iOneAccessWeComService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @Resource
    private IUserService userService;

    private static final String ONE_ACCESS_STATE = "oneaccessState";
    private static final String REFERENCE = "reference";


    /**
     * Login callback interface.
     */
    @GetResource(path = "/oneaccess/oauth2/callback", requiredLogin = false)
    @Operation(summary = "Login callback interface", description = "Accept the authorization "
        + "interface of OneAccess and call back the login")
    public void oauth2Callback(@RequestParam(name = "code") String code,
                               @RequestParam(name = "state") String state,
                               HttpServletResponse response)
        throws IOException {
        // Check state validity
        HttpSession session = HttpContextUtil.getSession(false);
        if (!oneAccessProperties.isEnabled()) {
            toResponseData("This system does not currently support OneAccess login", response);
            return;
        }

        if (session == null || !state.equals(session.getAttribute(ONE_ACCESS_STATE))) {
            toResponseData("state error", response);
            return;
        }
        try {
            // Single sign-on by code
            Long userId = oneAccessService.getUserIdByCode(code);
            if (userId == null) {
                toResponseData("Login failed, account does not exist", response);
                return;
            }
            SessionContext.setUserId(userId);
            userService.updateLoginTime(userId);
            // Redirect to reference address
            String redirectUrl = (String) session.getAttribute(REFERENCE);
            if (redirectUrl == null) {
                redirectUrl = constProperties.getServerDomain() + constProperties.getWorkbenchUrl();
            }
            redirectUrl = URLDecoder.decode(redirectUrl, "utf-8");
            response.sendRedirect(redirectUrl);
        } catch (BusinessException e) {
            toResponseData(e.getMessage(), response);
        }
    }

    /**
     * Login.
     */
    @GetResource(path = "/oneaccess/user/login", requiredLogin = false)
    @Operation(summary = "login", description = "Log in to the openaccess interface, redirect iam"
        + " single sign-on")
    @Deprecated
    public void userLogin(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        if (!oneAccessProperties.isEnabled()) {
            toResponseData("This system does not currently support OneAccess login", response);
            return;
        }
        HttpSession session = HttpContextUtil.getSession(true);
        // Set the redirect address after login
        String queryString = request.getQueryString();
        String defaultWorkUri =
            constProperties.getServerDomain() + constProperties.getWorkbenchUrl();
        if (queryString != null && queryString.startsWith("reference")) {
            defaultWorkUri = queryString.substring("reference".length() + 1);
            session.setAttribute(REFERENCE, defaultWorkUri);
        }
        // If you are currently logged in, jump directly to the default page
        if (session.getAttribute(SessionAttrConstants.LOGIN_USER_ID) != null) {
            defaultWorkUri = URLDecoder.decode(defaultWorkUri, "utf-8");
            response.sendRedirect(defaultWorkUri);
            return;
        }
        // Add state random code
        String state = RandomExtendUtil.randomString(8);
        session.setAttribute(ONE_ACCESS_STATE, state);

        // Add application information, jump to iam, example => https://{host}:{port}/idp/oauth2/authorize?redirect_uri=http://{host}:{port}/pubapp/oauth/callback&state=xxxx&client_id=xxxxx&response_type=code
        String redirectUri = StrUtil.format(oneAccessProperties.getIamHost()
                + "/idp/oauth2/authorize?redirect_uri={}/api/v1/social/oneaccess/oauth2/callback"
                + "&state={}&client_id={}&response_type=code",
            constProperties.getServerDomain(), state, oneAccessProperties.getClientId());
        try {
            response.sendRedirect(redirectUri);
        } catch (Exception e) {
            log.warn("userLogin redirect failed, redirectUri：{},err:{}", redirectUri,
                e.getMessage());
            toResponseData("Redirect OneAccess login failed", response);
        }
    }

    /**
     * Unified login interface.
     */
    @GetResource(path = "/oneaccess/login", requiredLogin = false)
    @Operation(summary = "Unified login interface", description = "login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!oneAccessProperties.isEnabled()) {
            toResponseData("This system does not currently support OneAccess login", response);
            return;
        }
        HttpSession session = HttpContextUtil.getSession(true);
        // Set the redirect address after login
        String queryString = request.getQueryString();
        String referenceUri = constProperties.getServerDomain() + "/workbench";
        if (queryString != null && queryString.startsWith("reference")) {
            referenceUri = queryString.substring("reference".length() + 1);
            referenceUri = URLDecoder.decode(referenceUri, "utf-8");
            // Restrict non-local system domain names from jumping to reference
            if (referenceUri.startsWith("http") && !referenceUri.startsWith(
                constProperties.getServerDomain())) {
                toResponseData("reference is an untrusted site, access is not allowed", response);
                return;
            }
            session.setAttribute(REFERENCE, referenceUri);
        }
        // If you are currently logged in, jump directly to the default page
        if (session.getAttribute(SessionAttrConstants.LOGIN_USER_ID) != null) {
            response.sendRedirect(referenceUri);
            return;
        }
        String ua = request.getHeader("User-Agent");
        log.info("ua=>{}", ua);
        // Set a single point of entry according to UserAgent
        if (ua.lastIndexOf("wxworklocal") != -1) {
            // Government Wecom Single Point
            String redirect = StrUtil.format(
                "{}/connect/oauth2/authorize?appid={}&redirect_uri={}/api/v1/social/oneaccess"
                    + "/wecom/login&response_type=code&scope=snsapi_base&agentid={}&state=STATE"
                    + "#wechat_redirect",
                oneAccessProperties.getWeCom().getBaseApiUrl(),
                oneAccessProperties.getWeCom().getCorpid(), constProperties.getServerDomain(),
                oneAccessProperties.getWeCom().getAgentId());
            response.sendRedirect(redirect);
        } else {
            // Add state random code
            String state = RandomExtendUtil.randomString(8);
            session.setAttribute(ONE_ACCESS_STATE, state);
            // Add application information, jump to iam, example => https://{host}:{port}/idp/oauth2/authorize?redirect_uri=http://{host}:{port}/pubapp/oauth/callback&state=xxxx&client_id=xxxxx&response_type=code
            String redirect = StrUtil.format(oneAccessProperties.getIamHost()
                    + "/idp/oauth2/authorize?redirect_uri={}/api/v1/social/oneaccess/oauth2"
                    + "/callback&state={}&client_id={}&response_type=code",
                constProperties.getServerDomain(), state, oneAccessProperties.getClientId());
            response.sendRedirect(redirect);
        }
    }

    /**
     * Government Affairs WeCom Login Interface.
     */
    @GetResource(path = "/oneaccess/wecom/login", requiredLogin = false)
    @Operation(summary = "Government Affairs WeCom Login Interface", description = "Government "
        + "WeCom Login")
    public void wecomLogin(@RequestParam(name = "code") String code, HttpServletResponse response)
        throws IOException {
        if (!oneAccessProperties.isEnabled()) {
            toResponseData("This system does not currently support government WeCom login",
                response);
            return;
        }
        HttpSession session = HttpContextUtil.getSession(true);
        // If you are currently logged in, jump directly to the default page
        String redirectUrl = (String) session.getAttribute(REFERENCE);
        if (redirectUrl == null) {
            redirectUrl = constProperties.getServerDomain() + constProperties.getWorkbenchUrl();
        }
        // Url decode,Compatible with web transcoding
        redirectUrl = URLDecoder.decode(redirectUrl, "utf-8");
        // Restrict non-local system domain names from jumping to reference
        if (redirectUrl.startsWith("http") && !redirectUrl.startsWith(
            constProperties.getServerDomain())) {
            toResponseData("reference is an untrusted site, access is not allowed", response);
            return;
        }
        // If you are currently logged in, jump directly to the default page
        if (session.getAttribute(SessionAttrConstants.LOGIN_USER_ID) != null) {
            response.sendRedirect(redirectUrl);
            return;
        }
        log.info("Government  WeCom application, request to log in code:{},redirectUrl:{}", code,
            redirectUrl);
        // Through the oauth user ID, corresponding to the LoginName of OneAccess
        String wxUserId = iOneAccessWeComService.getUserIdByOAuth2Code(code);
        // Single Sign-On government WeCom
        Long userId = oneAccessService.getUserIdByLoginNameAndUnionId(wxUserId, "wx" + wxUserId);
        ExceptionUtil.isNotNull(userId, USER_NOT_AUTH);
        SessionContext.setUserId(userId);
        userService.updateLoginTime(userId);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Copies a team or member to a designated space station.
     */
    @PostResource(path = "/oneaccess/copyTeamAndMembers")
    @Operation(summary = "Sync group or member to Honma station", description = "Synchronize a "
        + "group or member to the current station")
    public ResponseData<Void> copyTeamAndMembers(@RequestBody @Valid OneAccessCopyInfoRo body) {
        ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
        // Get Share Link Source Space Station
        String spaceId = iSpaceService.getSpaceIdByLinkId(body.getLinkId());
        String destSpaceId = LoginContext.me().getSpaceId();
        if (destSpaceId.equals(spaceId)) {
            return ResponseData.error(
                "The synchronized space and the target space cannot be the same");
        }
        // Only space administrator can synchronized
        Long userId = SessionContext.getUserId();
        UserSpaceDto userSpaceDto = userSpaceCacheService.getUserSpace(userId, destSpaceId);
        ExceptionUtil.isTrue(userSpaceDto.isMainAdmin() || userSpaceDto.isAdmin(),
            ONLY_TENANT_ADMIN_BOUND_ERROR);
        // Sync groups and people
        oneAccessService.copyTeamAndMembers(spaceId, destSpaceId, body.getTeamIds(),
            body.getMembers());
        return ResponseData.success();
    }

    /**
     * Get schema information.
     */
    @PostResource(path = "/oneaccess/SchemaService", requiredLogin = false)
    @Operation(summary = "Get schema information", description = "Get all information about "
        + "objects such as system account, institution role, etc.")
    public String userSchema(HttpServletRequest request) {
        OneAccessUserSchemaVo vo = new OneAccessUserSchemaVo("");
        try {
            // Check if the One Access feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // decrypt request
            OneAccessBaseRo ro = oneAccessService.getRequestObject(request, OneAccessBaseRo.class);
            // Returns the user sync data structure
            vo.setBimRequestId(ro.getBimRequestId());
            // Build OneAccess - User Data Source Structure Information
            AttributeEntity accountUid =
                AttributeEntity.builder().name("uid").required(true).type("string").build();
            AttributeEntity accountMobile =
                AttributeEntity.builder().name("mobile").required(true).type("string").build();
            AttributeEntity accountEmail =
                AttributeEntity.builder().name("email").required(true).type("string").build();
            AttributeEntity accountLoginName =
                AttributeEntity.builder().name("loginName").required(true).type("string").build();
            AttributeEntity accountFullName =
                AttributeEntity.builder().name("fullName").required(true).type("string").build();
            AttributeEntity accountOrgId =
                AttributeEntity.builder().name("orgId").required(true).type("string").build();
            AttributeEntity accountOneId =
                AttributeEntity.builder().name("oneId").required(false).type("oneId").build();
            List<AttributeEntity> accountEntities = new ArrayList<>();
            accountEntities.add(accountUid);
            accountEntities.add(accountMobile);
            accountEntities.add(accountEmail);
            accountEntities.add(accountLoginName);
            accountEntities.add(accountFullName);
            accountEntities.add(accountOrgId);
            accountEntities.add(accountOneId);
            // Build OneAccess - Organizational Schema Data Source Structure Information
            AttributeEntity orgId =
                AttributeEntity.builder().name("orgId").required(true).type("string").build();
            AttributeEntity orgName =
                AttributeEntity.builder().name("orgName").required(true).type("string").build();
            AttributeEntity parentOrgId =
                AttributeEntity.builder().name("parentOrgId").required(true).type("string").build();
            AttributeEntity orgCode =
                AttributeEntity.builder().name("orgCode").required(false).type("string").build();
            List<AttributeEntity> orgEntities = new ArrayList<>();
            orgEntities.add(orgId);
            orgEntities.add(orgName);
            orgEntities.add(parentOrgId);
            orgEntities.add(orgCode);

            vo.setAccount(accountEntities);
            vo.setOrganization(orgEntities);
        } catch (BusinessException e) {
            log.info("userSchema throw err:{}", e.getMessage());
            toEncryptResponseData(vo);
        }

        return toEncryptResponseData(vo);

    }

    /**
     * Account creation.
     */
    @PostResource(path = "/oneaccess/UserCreateService", requiredLogin = false)
    @Operation(summary = "Account creation", description = "The account is actively created by "
        + "the oneAccess platform")
    public String userCreate(HttpServletRequest request) {
        OneAccessCreateVo vo = new OneAccessCreateVo("");
        try {
            // Check if the OneAccess feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // decrypt request
            OneAccessUserCreateRo ro =
                oneAccessService.getRequestObject(request, OneAccessUserCreateRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify the authorized account and associate the space station bound to the
            // authorized account
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            // Create user parallel space
            Long uid = oneAccessService.createUser(spaceId, ro.getMobile(), ro.getLoginName(),
                ro.getFullName(), ro.getEmail(), ro.getOrgId(), ro.getOneId());
            vo.setUid(String.format("%d", uid));
            vo.setResultCode("0");
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        } catch (Exception e) {
            // Other non-BusinessException, unified conversion returns
            vo.setResultCode("500");
            vo.setMessage("mobile or email already exists");
            return toEncryptResponseData(vo);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * User update.
     */
    @PostResource(path = "/oneaccess/UserUpdateService", requiredLogin = false)
    @Operation(summary = "User update", description = "The user information is actively updated "
        + "by the OneAccess platform")
    public String userUpdate(HttpServletRequest request) {
        OneAccessBaseVo vo = new OneAccessBaseVo("");
        try {
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Decrypt update user request
            OneAccessUserUpdateRo ro =
                oneAccessService.getRequestObject(request, OneAccessUserUpdateRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify account ID
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            ExceptionUtil.isNotEmpty(ro.getBimUid(), USER_NOT_EXIST);
            long uid = Long.parseLong(ro.getBimUid());
            // Update user information
            boolean result =
                oneAccessService.updateUser(spaceId, uid, ro.getMobile(), ro.getLoginName(),
                    ro.getFullName(), ro.getEmail(), ro.getOrgId());
            ExceptionUtil.isTrue(result, TENANT_NOT_EXIST);
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        } catch (Exception e) {
            // Other non-BusinessException, unified conversion returns
            vo.setResultCode("500");
            vo.setMessage("mobile or email already exists");
            return toEncryptResponseData(vo);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * user delete.
     */
    @PostResource(path = "/oneaccess/UserDeleteService", requiredLogin = false)
    @Operation(summary = "user delete", description = "Delete the account by the oneAccess "
        + "platform")
    public String userDelete(HttpServletRequest request) {
        OneAccessBaseVo vo = new OneAccessBaseVo("");
        try {
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            //Decrypt request to delete user
            OneAccessUserDeleteRo ro =
                oneAccessService.getRequestObject(request, OneAccessUserDeleteRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify account ID
            ExceptionUtil.isNotEmpty(ro.getBimUid(), USER_NOT_EXIST);
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            long userId = Long.parseLong(ro.getBimUid());
            oneAccessService.deleteUser(spaceId, userId);
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * Queries the detailed information of the account.
     *
     * @param request
     * @return userInfo
     */
    @PostResource(path = "/oneaccess/QueryUserByIdService", requiredLogin = false)
    @Operation(summary = "query user", description = "query user by params")
    public String queryUserByIdService(HttpServletRequest request) {
        OneAccessUserQueryVo vo = new OneAccessUserQueryVo("");
        try {
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            //Decrypt request to delete user
            OneAccessUserQueryRo ro =
                oneAccessService.getRequestObject(request, OneAccessUserQueryRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify account ID
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            long userId = Long.parseLong(ro.getBimUid());
            vo = oneAccessService.getUserById(spaceId, userId, ro.getOneId());
            vo.setBimRequestId(ro.getBimRequestId());
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }
        return toEncryptResponseData(vo);
    }

    /**
     * organization creation.
     */
    @PostResource(path = "/oneaccess/OrgCreateService", requiredLogin = false)
    @Operation(summary = "organization creation", description = "Organizations are created by "
        + "oneAccess")
    public String orgCreate(HttpServletRequest request) {
        OneAccessCreateVo vo = new OneAccessCreateVo("");
        try {
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Decrypt the request to create an organization
            OneAccessOrgCreateRo ro =
                oneAccessService.getRequestObject(request, OneAccessOrgCreateRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            // Create Space Station Organizations
            Long orgId = oneAccessService.createOrg(spaceId, ro.getOrgName(), ro.getOrgId(),
                ro.getParentOrgId());
            vo.setUid(String.format("%d", orgId));
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * Organizational update.
     */
    @PostResource(path = "/oneaccess/OrgUpdateService", requiredLogin = false)
    @Operation(summary = "Organizational update", description = "Organizations are updated by "
        + "OneAccess")
    public String orgUpdate(HttpServletRequest request) {
        OneAccessBaseVo vo = new OneAccessBaseVo("");
        try {
            // Check if the One Access feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Decrypt the request to update the user
            OneAccessOrgUpdateRo ro =
                oneAccessService.getRequestObject(request, OneAccessOrgUpdateRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify authorized account
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            // update organization
            oneAccessService.updateOrg(ro.getBimOrgId(), spaceId, ro.getOrgId(), ro.getOrgName(),
                ro.getParentOrgId(), ro.is__ENABLE__());
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * Organization deletion.
     */
    @PostResource(path = "/oneaccess/OrgDeleteService", requiredLogin = false)
    @Operation(summary = "Organization deletion", description = "Active deletion of an "
        + "organization by OneAccess")
    public String orgDelete(HttpServletRequest request) {
        OneAccessBaseVo vo = new OneAccessBaseVo("");
        try {
            // Check if the One Access feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Decrypt request to delete org
            OneAccessOrgDeleteRo ro =
                oneAccessService.getRequestObject(request, OneAccessOrgDeleteRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify account ID
            ExceptionUtil.isNotEmpty(ro.getBimOrgId(), GET_TEAM_ERROR);
            // Verify authorized account
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            // Renewal Organization, Associated Space Station
            oneAccessService.deleteOrg(spaceId, ro.getBimOrgId());
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }

        return toEncryptResponseData(vo);
    }

    /**
     * Queries the detailed information of the org.
     *
     * @param request
     * @return org info
     */
    @PostResource(path = "/oneaccess/QueryOrgByIdService", requiredLogin = false)
    @Operation(summary = "query org", description = "query org by params")
    public String queryOrgByIdService(HttpServletRequest request) {
        OneAccessOrgVo vo = new OneAccessOrgVo("");
        try {
            // Check if the One Access feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Check if the One Access feature is enabled
            ExceptionUtil.isTrue(oneAccessProperties.isEnabled(), AGENT_CONFIG_DISABLE);
            // Decrypt request to delete org
            OneAccessOrgQueryRo ro =
                oneAccessService.getRequestObject(request, OneAccessOrgQueryRo.class);
            vo.setBimRequestId(ro.getBimRequestId());
            // Verify authorized account
            String spaceId = oneAccessService.getSpaceIdByBimAccount(ro.getBimRemoteUser(),
                ro.getBimRemotePwd());
            vo = oneAccessService.getOrgById(spaceId, ro.getBimOrgId(), ro.getOpenOrgId());
            vo.setBimRequestId(ro.getBimRequestId());
        } catch (BusinessException e) {
            return toErrorResponseData(vo, e);
        }
        return toEncryptResponseData(vo);
    }

    /**
     * output page return information.
     */
    private void toResponseData(String data, HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        out.write(data.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * toErrorResponseData.
     */
    private String toErrorResponseData(OneAccessBaseVo vo, BusinessException err) {
        vo.setResultCode(String.valueOf(err.getCode()));
        vo.setMessage(err.getMessage());
        return toEncryptResponseData(vo);
    }

    /**
     * toEncryptResponseData.
     */
    private String toEncryptResponseData(Object vo) {
        String jsonStr = JSONUtil.toJsonStr(vo);
        return AESCipher.encrypt(jsonStr, oneAccessProperties.getEncryptKey());
    }
}
