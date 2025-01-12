/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.elink.controller;

import com.apitable.auth.enums.AuthException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.enterprise.elink.infrastructure.ElinkConnector;
import com.apitable.enterprise.elink.infrastructure.ElinkTemplate.AgentApp;
import com.apitable.enterprise.elink.model.ElinkUnitDTO;
import com.apitable.enterprise.elink.model.ElinkUserDTO;
import com.apitable.enterprise.elink.service.ElinkService;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.constants.SessionAttrConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Elink")
@ApiResource(path = "/social")
@Slf4j
@ConditionalOnProperty(value = "connector.elink.enabled", havingValue = "true")
public class ElinkController {

    @Autowired(required = false)
    private ElinkConnector ElinkConnector;


    @Autowired(required = false)
    private ElinkService ElinkService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @GetResource(path = "/elink/login", requiredLogin = false)
    @Operation(description = "Use auth.code to log in synchronously")
    public ResponseData<String> ElinkloginByCode(
        @RequestParam(name = "code") String code,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        HttpSession session = HttpContextUtil.getSession(true);
        String host = request.getHeader("Host");
        AgentApp agentApp = ElinkConnector.getAgentAppByHost(host);
        //Login and create user
        String openId = ElinkConnector.getUserIdByCode(agentApp, code);
        ExceptionUtil.isNotEmpty(openId, SocialException.GET_USER_INFO_ERROR);

        Long userId = iSocialUserBindService.getUserIdByUnionId(openId);
        if (userId == null) {
            userId = iUserService.createByExternalSystem(openId, "", "", "", "");
        }
        // Update last login time
        iUserService.updateLoginTime(userId);
        // Save session
        SessionContext.setUserId(userId);
        //Set the login redirect address, if you have logged in, you will jump directly
        String redirectUrl = (String) session.getAttribute("reference");
        if (redirectUrl == null) {
            //Obtain the domain name associated with the Host
            redirectUrl = agentApp.getCallbackDomain() + "/workbench";
        }
        redirectUrl = java.net.URLDecoder.decode(redirectUrl, "utf-8");
        response.sendRedirect(redirectUrl);
        return ResponseData.success(null);
    }

    @GetResource(path = "/elink/login/new", requiredLogin = false)
    public void Elinklogin(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        HttpSession session = HttpContextUtil.getSession(true);
        //Set the redirect address after login.
        String queryString = request.getQueryString();
        String host = request.getHeader("Host");
        AgentApp agentApp = ElinkConnector.getAgentAppByHost(host);
        String referenceUri = java.net.URLDecoder.decode(agentApp.getCallbackDomain(), "utf-8");
        if (queryString != null && queryString.startsWith("reference")) {
            referenceUri = queryString.substring("reference".length() + 1);
            session.setAttribute("reference", referenceUri);
        }
        //If you are currently logged in, jump directly to the default page
        if (session.getAttribute(SessionAttrConstants.LOGIN_USER_ID) != null) {
            response.sendRedirect(referenceUri);
            return;
        }
        String ua = request.getHeader("User-Agent");
        log.info("ua=>{}", ua);
        //Set a single point of entry according to ua
        if (ua.lastIndexOf("wxworklocal") == -1) {
            String str =
                this.ElinkConnector.buildQRRedirectUrl(agentApp, "/api/v1/social/elink/login");
            response.sendRedirect(str);
            return;
        }
        String redirect = ElinkConnector.buildRedirectUrl(agentApp, "/api/v1/social/elink/login");
        response.sendRedirect(redirect);
    }

    @PostResource(path = "/elink/saveOrUpdateUser")
    @Operation(summary = "Add or update users")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "Space ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
    })
    public ResponseData<Void> saveOrUpdateUser(@RequestBody @Valid ElinkUserDTO body,
                                               @RequestHeader(name = ParamsConstants.SPACE_ID)
                                               String spaceId) {
        //check permissions
        Long memberId = LoginContext.me().getMemberId();
        ExceptionUtil.isFalse(memberId.equals(iSpaceService.getSpaceMainAdminMemberId(spaceId)),
            AuthException.FORBIDDEN);
        ElinkService.saveOrUpdateUser(spaceId, body);
        return ResponseData.success();
    }

    @PostResource(path = "/elink/deleteUser/{openId}")
    @Operation(summary = "delete user")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID,
            description = "Space ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
    })
    public ResponseData<Void> deleteUser(@PathVariable("openId") String openId,
                                         @RequestHeader(name = ParamsConstants.SPACE_ID)
                                         String spaceId) {
        //Check permissions
        Long memberId = LoginContext.me().getMemberId();
        ExceptionUtil.isFalse(memberId.equals(iSpaceService.getSpaceMainAdminMemberId(spaceId)),
            AuthException.FORBIDDEN);
        ElinkService.deleteUser(spaceId, openId);
        return ResponseData.success();
    }

    @PostResource(path = "/elink/saveOrUpdateUnit")
    @Operation(description = "Add or update organizations")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "Space ID", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
    })
    public ResponseData<Void> saveOrUpdateUnit(@RequestBody @Valid ElinkUnitDTO body,
                                               @RequestHeader(name = ParamsConstants.SPACE_ID)
                                               String spaceId) {
        //Check permissions
        Long memberId = LoginContext.me().getMemberId();
        ExceptionUtil.isFalse(memberId.equals(iSpaceService.getSpaceMainAdminMemberId(spaceId)),
            AuthException.FORBIDDEN);
        ElinkService.saveOrUpdateUnit(spaceId, body);
        return ResponseData.success();
    }

    @PostResource(path = "/elink/deleteUnit/{unitId}")
    @Operation(summary = "delete organization")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "Space ID",
            required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER,
            example = "spcyQkKp9XJEl"),
    })
    public ResponseData<Void> deleteUnit(@PathVariable("unitId") String unitId,
                                         @RequestHeader(name = ParamsConstants.SPACE_ID)
                                         String spaceId) {
        //Check permissions
        Long memberId = LoginContext.me().getMemberId();
        ExceptionUtil.isFalse(memberId.equals(iSpaceService.getSpaceMainAdminMemberId(spaceId)),
            AuthException.FORBIDDEN);
        ElinkService.deleteUnit(spaceId, unitId);
        return ResponseData.success();
    }
}