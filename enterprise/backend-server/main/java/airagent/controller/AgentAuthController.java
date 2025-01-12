package com.apitable.enterprise.airagent.controller;

import static com.apitable.enterprise.auth0.model.Auth0Constants.UnAuthorizedError;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.auth.vo.LogoutVO;
import com.apitable.enterprise.airagent.autoconfigure.AirAgentProperties;
import com.apitable.enterprise.airagent.service.IAgentAuthService;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.model.Auth0UserProfile;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.config.properties.CookieProperties;
import com.apitable.shared.context.SessionContext;
import com.auth0.Tokens;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * air-agent auth controller.
 */
@RestController
@Tag(name = "AirAgent - Auth")
@ApiResource
@Slf4j
public class AgentAuthController {

    @Resource
    private IAgentAuthService iAgentAuthService;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private CookieProperties cookieProperties;

    @Resource
    private AirAgentProperties airAgentProperties;

    @Autowired(required = false)
    private Auth0Template auth0Template;

    /**
     * login api.
     */
    @GetResource(path = "/airagent/login", requiredLogin = false)
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
     * auth0 user login callback.
     *
     * @param code             auth0 code
     * @param error            auth0 error
     * @param errorDescription auth0 error description
     * @return redirect view
     * @throws IOException io exception
     */
    @GetResource(path = "/airagent/login/callback", requiredLogin = false)
    @Operation(hidden = true)
    public RedirectView callback(
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "error", required = false) String error,
        @RequestParam(name = "error_description", required = false) String errorDescription
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
            Long userId = iAgentAuthService.createUserIfNotExist(user);
            // save session
            SessionContext.setUserId(userId);
            // redirect home page
            String redirectUrl =
                constProperties.getServerDomain() + airAgentProperties.getHomePagePath();
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
     * logout router.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return {@link LogoutVO}
     */
    @PostResource(path = "/airagent/logout", requiredPermission = false, method = {
        RequestMethod.GET,
        RequestMethod.POST}, requiredLogin = false)
    @Operation(summary = "Logout", description = "logout current user")
    public RedirectView logout(final HttpServletRequest request,
                               final HttpServletResponse response) {
        SessionContext.cleanContext(request);
        SessionContext.removeCookie(response,
            cookieProperties.getI18nCookieName(),
            cookieProperties.getDomainName());
        String redirectUri = auth0Template.buildLogoutUrl(constProperties.getServerDomain());
        return new RedirectView(redirectUri);
    }
}
