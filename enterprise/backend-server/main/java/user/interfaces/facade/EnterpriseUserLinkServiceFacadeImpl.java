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

package com.apitable.enterprise.user.interfaces.facade;

import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.interfaces.user.facade.UserLinkServiceFacade;
import com.apitable.interfaces.user.model.UserLinkRequest;
import com.apitable.shared.cache.bean.SocialAuthInfo;
import com.apitable.shared.cache.bean.UserLinkInfo;

public class EnterpriseUserLinkServiceFacadeImpl implements UserLinkServiceFacade {

    private final IUserLinkService iUserLinkService;

    public EnterpriseUserLinkServiceFacadeImpl(IUserLinkService iUserLinkService) {
        this.iUserLinkService = iUserLinkService;
    }

    @Override
    public void createUserLink(UserLinkRequest userLinkRequest) {
        iUserLinkService.createUserLink(userLinkRequest.getUserId(), userLinkRequest.getAuthInfo());
    }

    @Override
    public void wrapperSocialAuthInfo(SocialAuthInfo authInfo) {
        iUserLinkService.wrapperSocialAuthInfo(authInfo);
    }

    @Override
    public UserLinkInfo getUserLinkInfo(Long userId) {
        return iUserLinkService.getUserLinkInfo(userId);
    }
}
