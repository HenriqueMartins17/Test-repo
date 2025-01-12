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

package com.apitable.enterprise.auth0.interfaces.facade;

import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.interfaces.auth.facade.AuthServiceFacade;
import com.apitable.interfaces.auth.model.AuthParam;
import com.apitable.interfaces.auth.model.UserAuth;
import com.apitable.interfaces.auth.model.UserLogout;

/**
 * auth facade implement by auth0.
 */
public class Auth0AuthServiceFacadeImpl implements AuthServiceFacade {

    private final Auth0Service auth0Service;

    /**
     * Auth0AuthServiceFacadeImpl Constructor.
     *
     * @param auth0Service {@link Auth0Service}
     */
    public Auth0AuthServiceFacadeImpl(Auth0Service auth0Service) {
        this.auth0Service = auth0Service;
    }

    /**
     * user login.
     *
     * @param param login param
     * @return {@link UserAuth}
     */
    @Override
    public UserAuth ssoLogin(AuthParam param) {
        return null;
    }

    /**
     * user logs out.
     *
     * @param userAuth {@link UserAuth}
     * @return {@link UserLogout}
     */
    @Override
    public UserLogout logout(UserAuth userAuth) {
        String redirectUri = auth0Service.logout();
        return new UserLogout(redirectUri);
    }
}
