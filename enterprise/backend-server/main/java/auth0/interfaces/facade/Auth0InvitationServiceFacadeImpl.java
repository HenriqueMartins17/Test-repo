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
import com.apitable.interfaces.user.facade.InvitationServiceFacade;
import com.apitable.interfaces.user.model.InvitationMetadata;
import com.apitable.interfaces.user.model.MultiInvitationMetadata;

/**
 * invitation facade implement by auth0.
 */
public class Auth0InvitationServiceFacadeImpl implements InvitationServiceFacade {

    private final Auth0Service auth0Service;

    public Auth0InvitationServiceFacadeImpl(Auth0Service auth0Service) {
        this.auth0Service = auth0Service;
    }

    @Override
    public void sendInvitationEmail(InvitationMetadata metadata) {
        auth0Service.sendInvitationEmail(metadata.getInviteUserId(), metadata.getSpaceId(),
            metadata.getEmail());
    }

    @Override
    public void sendInvitationEmail(MultiInvitationMetadata metadata) {
        auth0Service.sendInvitationEmail(metadata.getInviteUserId(), metadata.getSpaceId(),
            metadata.getEmails());
    }
}
