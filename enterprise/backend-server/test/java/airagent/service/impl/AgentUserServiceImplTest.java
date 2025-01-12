package com.apitable.enterprise.airagent.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.airagent.model.UserProfile;
import org.junit.jupiter.api.Test;

public class AgentUserServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetUserProfile() {
        AgentUserContext userContext = createAgentUser();
        UserProfile userProfile = iAgentUserService.getUserProfile(userContext.getUserId());
        assertThat(userProfile).isNotNull();
    }
}
