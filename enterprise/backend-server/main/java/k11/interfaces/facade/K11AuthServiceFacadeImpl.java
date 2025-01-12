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

package com.apitable.enterprise.k11.interfaces.facade;

import com.apitable.enterprise.k11.service.K11Service;
import com.apitable.interfaces.auth.facade.AuthServiceFacade;
import com.apitable.interfaces.auth.model.AuthParam;
import com.apitable.interfaces.auth.model.UserAuth;
import com.apitable.interfaces.auth.model.UserLogout;

public class K11AuthServiceFacadeImpl implements AuthServiceFacade {

    private final K11Service k11Service;

    public K11AuthServiceFacadeImpl(K11Service k11Service) {
        this.k11Service = k11Service;
    }

    @Override
    public UserAuth ssoLogin(AuthParam param) {
        Long userId = k11Service.loginBySso(param.getUsername(), param.getPassword());
        return new UserAuth(userId);
    }

    @Override
    public UserLogout logout(UserAuth userAuth) {
        return null;
    }
}
