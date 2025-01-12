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

package com.apitable.enterprise.auth0.controller;

import static com.apitable.enterprise.auth0.model.Auth0Constants.UnAuthorizedError;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.model.Auth0User;
import com.apitable.enterprise.auth0.model.Auth0UserProfile;
import com.apitable.enterprise.auth0.model.UserSpaceDTO;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.HttpServletUtil;
import com.auth0.Tokens;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * auth0 controller.
 *
 * @author Shawn Deng
 */
@RestController
@ApiResource
@Tag(name = "Auth0 API")
@Slf4j
public class Auth0Controller {

    @Resource
    private Auth0Service auth0Service;

    @Resource
    private ConstProperties constProperties;

    @Autowired(required = false)
    private Auth0Template auth0Template;

    /**
     * login api.
     */
    @GetResource(path = "/auth0/login", requiredLogin = false)
    @Operation(hidden = true)
    public RedirectView login(@RequestParam(name = "message", required = false) String message,
                              HttpServletRequest request) {
        if (auth0Template == null) {
            log.error("auth0 component is disabled");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String queryString = StrUtil.isBlank(message) ? request.getQueryString() : StrUtil.EMPTY;
        try {
            String authorizeUrl = auth0Template.buildAuthorizeUrl(queryString);
            if (log.isDebugEnabled()) {
                log.debug("authorize redirect url is {}", authorizeUrl);
            }
            return new RedirectView(authorizeUrl);
        } catch (Exception e) {
            log.error("fail to build authorize url", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * login callback api.
     */
    @GetResource(path = "/auth0/callback", requiredLogin = false)
    @Operation(hidden = true)
    public RedirectView callback(
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "error", required = false) String error,
        @RequestParam(name = "error_description", required = false) String errorDescription,
        HttpServletRequest request
    ) throws IOException {
        if (auth0Template == null) {
            log.error("auth0 component is disabled");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (StrUtil.isNotBlank(error) && StrUtil.isNotBlank(errorDescription)) {
            // error callback
            if (UnAuthorizedError.equals(error)) {
                // check if the verified email is expired,then send it again
                return new RedirectView(
                    constProperties.getServerDomain() + constProperties.getEmailVerificationUrl());
            } else {
                log.error("can't determine auth0 callback error");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        try {
            Tokens tokens = auth0Template.getVerifiedTokens(code, auth0Template.getRedirectUri());
            DecodedJWT idToken = JWT.decode(tokens.getIdToken());
            Auth0UserProfile user = Auth0UserProfile.claimsAsJson(idToken);
            if (log.isDebugEnabled()) {
                log.debug("user info: {}", JSONUtil.toJsonPrettyStr(user));
            }
            // save user if user does not exist
            Map<String, String> externalProperty = HttpServletUtil.getParameterAsMap(request, true);
            Auth0User auth0User = auth0Service.createUserByAuth0IfNotExist(user, externalProperty);
            // save session
            SessionContext.setUserId(auth0User.getUserId());
            String redirectUrl =
                constProperties.getServerDomain() + constProperties.getWorkbenchUrl();
            if (CollUtil.isNotEmpty(auth0User.getQueryString())) {
                redirectUrl += "?" + CollUtil.join(auth0User.getQueryString(), "&");
            }
            return new RedirectView(redirectUrl);
        } catch (APIException e) {
            log.error("Error Request Api", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Auth0Exception e) {
            log.error("Error trying to verify identity", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Auth Internal Service Error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * invitation callback.
     */
    @GetResource(path = "/invitation/callback", requiredLogin = false)
    @Operation(hidden = true)
    public RedirectView invitationCallback(@RequestParam(name = "email") String email,
                                           @RequestParam(name = "success") boolean success) {
        if (!success) {
            return new RedirectView("/error/404", true);
        }
        User user;
        try {
            user = auth0Template.usersByEmail(email);
        } catch (Auth0Exception e) {
            log.error("can't find user with this email", e);
            return new RedirectView("/error/404", true);
        }
        if (user == null) {
            // return error page
            return new RedirectView("/error/404", true);
        }
        UserSpaceDTO userSpaceDTO = auth0Service.createUserByAuth0IfNotExist(user);
        // save session
        SessionContext.setUserId(userSpaceDTO.getUserId());
        return new RedirectView(
            constProperties.getServerDomain() + constProperties.getWorkbenchUrl());
    }

}
