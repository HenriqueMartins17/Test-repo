package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.auth0.model.Auth0UserProfile;

/**
 * agent auth service interface.
 */
public interface IAgentAuthService {

    /**
     * create user if not exist.
     *
     * @param userProfile userProfile
     * @return userId
     */
    Long createUserIfNotExist(Auth0UserProfile userProfile);
}
