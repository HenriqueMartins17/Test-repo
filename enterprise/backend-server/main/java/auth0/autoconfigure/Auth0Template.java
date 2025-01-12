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

package com.apitable.enterprise.auth0.autoconfigure;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.Tokens;
import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.EmailVerificationIdentity;
import com.auth0.json.mgmt.tickets.PasswordChangeTicket;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.AuthRequest;
import com.auth0.utils.tokens.IdTokenVerifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * auth0 template.
 */
@Slf4j
public class Auth0Template {

    private final AuthAPI authAPI;

    private ManagementAPI managementAPI;

    private final String domain;

    private final String audience;

    private final String clientId;

    private final String redirectUri;

    private final String dbConnectionName;

    private final IdTokenVerifier tokenVerifier;

    public String getDbConnectionName() {
        return dbConnectionName;
    }

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(
        1);

    /**
     * auth0 template constructor.
     *
     * @param domain           domain
     * @param clientId         client Id
     * @param clientSecret     client secrete
     * @param audience         audience
     * @param redirectUri      redirect uri
     * @param dbConnectionName connection name
     * @param tokenVerifier    token verifier
     */
    public Auth0Template(String domain, String clientId, String clientSecret,
                         String audience, String redirectUri,
                         String dbConnectionName, IdTokenVerifier tokenVerifier) {
        authAPI = new AuthAPI(domain, clientId, clientSecret);
        this.domain = domain;
        this.audience = audience;
        refreshManagementAPIToken();
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.dbConnectionName = dbConnectionName;
        this.tokenVerifier = tokenVerifier;
        scheduled.scheduleAtFixedRate(this::refreshManagementAPIToken, 10, 1,
            TimeUnit.HOURS);
    }

    private void refreshManagementAPIToken() {
        AuthRequest authRequest = authAPI.requestToken(audience);
        TokenHolder holder;
        try {
            holder = authRequest.execute();
        } catch (Auth0Exception e) {
            throw new IllegalStateException(
                "can't initial auth0 management api instance", e);
        }
        this.managementAPI = new ManagementAPI(domain, holder.getAccessToken());
    }

    /**
     * build authorize url.
     *
     * @param sourceQueryString source query string
     * @return authorize url
     */
    public String buildAuthorizeUrl(String sourceQueryString) {
        return authAPI.authorizeUrl(
                StrUtil.isNotBlank(sourceQueryString) ? redirectUri.concat("?" + sourceQueryString) :
                    redirectUri)
            .withScope("openid profile email").build();
    }

    public String buildLogoutUrl(String returnToUrl) {
        return authAPI.logoutUrl(returnToUrl, true).useFederated(false).build();
    }

    /**
     * get verified token.
     *
     * @param authorizationCode authorization code
     * @param redirectUri       redirect uri
     * @return {@link Tokens}
     * @throws Auth0Exception exception
     */
    public Tokens getVerifiedTokens(String authorizationCode,
                                    String redirectUri) throws Auth0Exception {
        Tokens codeExchangeTokens = exchangeCodeForTokens(authorizationCode,
            redirectUri);
        String idTokenFromCodeExchange = codeExchangeTokens.getIdToken();
        if (idTokenFromCodeExchange != null) {
            tokenVerifier.verify(idTokenFromCodeExchange);
        }
        return codeExchangeTokens;
    }

    /**
     * exchange code fot token.
     *
     * @param authorizationCode authorization code
     * @param redirectUri       redirect uri
     * @return {@link Tokens}
     * @throws Auth0Exception exception
     */
    public Tokens exchangeCodeForTokens(String authorizationCode,
                                        String redirectUri) throws Auth0Exception {
        TokenHolder holder = authAPI.exchangeCode(authorizationCode, redirectUri).execute();
        return new Tokens(holder.getAccessToken(), holder.getIdToken(),
            holder.getRefreshToken(), holder.getTokenType(),
            holder.getExpiresIn());
    }

    /**
     * retrieve user.
     *
     * @param userId auth0 user id
     * @return Auth0 User
     */
    public Optional<User> getUser(String userId) {
        try {
            return Optional.ofNullable(managementAPI.users().get(userId, null).execute());
        } catch (Auth0Exception e) {
            return Optional.empty();
        }
    }

    /**
     * get user app metadata.
     *
     * @param userId auth0 user id
     * @return app metadata
     */
    public Map<String, Object> getUserAppMetadata(String userId) {
        Optional<User> userOptional = getUser(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getAppMetadata() != null ? user.getAppMetadata() : Collections.emptyMap();
        }
        return Collections.emptyMap();
    }

    /**
     * get user by email.
     *
     * @param email email
     * @return {@link User}
     * @throws Auth0Exception exception
     */
    public User usersByEmail(String email) throws Auth0Exception {
        List<User> users = managementAPI.users().listByEmail(email, null)
            .execute();
        if (users != null && !users.isEmpty()) {
            return users.iterator().next();
        }
        return null;
    }

    /**
     * create user by email.
     *
     * @param email           email
     * @param sendVerifyEmail whether send verify email
     * @return user id
     * @throws Auth0Exception exception
     */
    public String createUser(String email, boolean sendVerifyEmail)
        throws Auth0Exception {
        User request = new User();
        request.setEmail(email);
        request.setPassword(UUID.randomUUID().toString().toCharArray());
        request.setVerifyEmail(sendVerifyEmail);
        request.setConnection(dbConnectionName);
        User user = managementAPI.users().create(request).execute();
        return user.getId();
    }

    /**
     * create user by email.
     *
     * @param email           email
     * @param sendVerifyEmail whether send verify email
     * @return user id
     * @throws Auth0Exception exception
     */
    public User createUser(String email, String password, Boolean sendVerifyEmail)
        throws Auth0Exception {
        User request = new User();
        request.setEmail(email);
        request.setPassword(password.toCharArray());
        request.setVerifyEmail(sendVerifyEmail);
        request.setConnection(dbConnectionName);
        return managementAPI.users().create(request).execute();
    }

    /**
     * Links two User's Identities.
     *
     * @param primaryUserId   the primary identity's user id
     * @param secondaryUserId the secondary identity's user id
     * @param provider        the provider name of the secondary identity.
     * @param connectionId    the connection id of the secondary account being
     *                        linked, useful if the provider is 'auth0' and you
     *                        have several connections. Can be null.
     */
    public void linkIdentity(String primaryUserId,
                             String secondaryUserId, String provider, String connectionId)
        throws Auth0Exception {
        managementAPI.users()
            .linkIdentity(primaryUserId, secondaryUserId, provider,
                connectionId).execute();

    }

    /**
     * create reset password ticket.
     *
     * @param userId    user id
     * @param returnUrl url
     * @return String
     * @throws Auth0Exception exception
     */
    public String createPasswordResetTicket(String userId, String returnUrl)
        throws Auth0Exception {
        PasswordChangeTicket request = new PasswordChangeTicket(userId);
        request.setResultUrl(returnUrl);
        request.setIncludeEmailInRedirect(true);
        request.setMarkEmailAsVerified(true);
        PasswordChangeTicket ticket = managementAPI.tickets()
            .requestPasswordChange(request).execute();
        return ticket.getTicket();
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void resetPassword(String email) throws Auth0Exception {
        authAPI.resetPassword(email, this.dbConnectionName).execute();
    }

    public void sendVerificationEmail(String userId, EmailVerificationIdentity identity)
        throws Auth0Exception {
        managementAPI.jobs().sendVerificationEmail(userId, this.clientId, identity).execute();
    }

    /**
     * create user by email.
     *
     * @param userId   auth0 user id
     * @param userInfo update info
     * @return {@link User}
     * @throws Auth0Exception exception
     */
    public User updateUser(String userId, User userInfo) throws Auth0Exception {
        return managementAPI.users().update(userId, userInfo).execute();
    }

    /**
     * delete a user.
     *
     * @param userId user id
     */
    public void deleteUser(String userId) {
        managementAPI.users().delete(userId);
    }

    /**
     * get users by email, sort by create time.
     *
     * @param email email
     * @return users
     */
    public List<User> usersByEmailSortByCreatedAt(String email) {
        try {
            List<User> users = managementAPI.users().listByEmail(email, null)
                .execute();
            if (CollectionUtil.isNotEmpty(users)) {
                users.sort(Comparator.comparing(User::getCreatedAt));
            }
            return users;
        } catch (Auth0Exception e) {
            log.error("get users by email error", e);
            return null;
        }
    }
}
