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

package com.apitable.enterprise.auth0.instrument.web;

import com.apitable.enterprise.auth0.interfaces.facade.Auth0AuthServiceFacadeImpl;
import com.apitable.enterprise.auth0.interfaces.facade.Auth0InvitationServiceFacadeImpl;
import com.apitable.enterprise.auth0.interfaces.facade.Auth0UserServiceFacadeImpl;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.interfaces.auth.facade.AuthServiceFacade;
import com.apitable.interfaces.user.facade.InvitationServiceFacade;
import com.apitable.interfaces.user.facade.UserServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * auth0 integration with context config.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "auth0.enabled", havingValue = "true")
public class Auth0DomainContextConfig {

    @Bean
    @Primary
    public InvitationServiceFacade auth0InvitationServiceFacade(Auth0Service auth0Service) {
        return new Auth0InvitationServiceFacadeImpl(auth0Service);
    }

    @Bean
    @Primary
    public AuthServiceFacade auth0AuthServiceFacade(Auth0Service auth0Service) {
        return new Auth0AuthServiceFacadeImpl(auth0Service);
    }

    @Bean
    @Primary
    public UserServiceFacade auth0UserServiceFacade(Auth0Service auth0Service) {
        return new Auth0UserServiceFacadeImpl(auth0Service);
    }
}
