package com.apitable.enterprise.auth0.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.auth0.model.Auth0User;
import com.apitable.enterprise.auth0.model.Auth0UserProfile;
import com.auth0.json.mgmt.users.User;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class Auth0ServiceTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testCreateUserByAuth0IfNotExist() {
        Auth0UserProfile auth0UserProfile = new Auth0UserProfile();
        auth0UserProfile.setNickname("auth0 user");
        auth0UserProfile.setEmail("auth0@test.com");
        auth0UserProfile.setSub("google-oauth2|" + UUID.randomUUID());

        Map<String, Object> appMetadata = new HashMap<>(1);
        appMetadata.put("template", "ai_onboarding");
        User user = new User();
        user.setAppMetadata(appMetadata);
        List<User> mockUsers = createMockUsers(1);

        doReturn(appMetadata).when(auth0Template).getUserAppMetadata(anyString());
        doReturn(mockUsers).when(auth0Template).usersByEmailSortByCreatedAt(anyString());
        Auth0User auth0User =
            auth0Service.createUserByAuth0IfNotExist(auth0UserProfile, Maps.newHashMap());
        assertThat(auth0User).isNotNull();
        assertThat(auth0User.getUserId()).isNotNull();
        assertThat(auth0User.getQueryString()).isNotEmpty();
    }

    @Test
    void testCreateUserByAuth0IfNotExistWithRewardFul() {
        Auth0UserProfile auth0UserProfile = new Auth0UserProfile();
        auth0UserProfile.setNickname("auth0 user");
        auth0UserProfile.setEmail("auth0@test.com");
        auth0UserProfile.setSub("google-oauth2|" + UUID.randomUUID());

        Map<String, Object> appMetadata = new HashMap<>(1);
        appMetadata.put("template", "ai_onboarding");
        User user = new User();
        user.setAppMetadata(appMetadata);
        List<User> mockUsers = createMockUsers(1);

        doReturn(appMetadata).when(auth0Template).getUserAppMetadata(anyString());
        doReturn(mockUsers).when(auth0Template).usersByEmailSortByCreatedAt(anyString());
        Map<String, String> externalProperty = Maps.newHashMap();
        String via = "mio";
        externalProperty.put("via", via);
        String referral = UUID.randomUUID().toString();
        externalProperty.put("referral", referral);
        String coupon = "fwkStE24";
        externalProperty.put("coupon", coupon);
        Auth0User auth0User =
            auth0Service.createUserByAuth0IfNotExist(auth0UserProfile, externalProperty);
        assertThat(auth0User).isNotNull();
        assertThat(auth0User.getUserId()).isNotNull();
        assertThat(auth0User.getQueryString()).isNotEmpty()
            .contains("via=" + via,
                "referral=" + referral,
                "coupon=" + coupon,
                "template=ai_onboarding");
    }

    @Test
    void testCreateUserByAuth0IfNotExistWithReferenceParameter() {
        Auth0UserProfile auth0UserProfile = new Auth0UserProfile();
        auth0UserProfile.setNickname("auth0 user");
        auth0UserProfile.setEmail("auth0@test.com");
        auth0UserProfile.setSub("google-oauth2|" + UUID.randomUUID());

        Map<String, Object> appMetadata = new HashMap<>(1);
        appMetadata.put("template", "ai_onboarding");
        User user = new User();
        user.setAppMetadata(appMetadata);
        List<User> mockUsers = createMockUsers(1);

        doReturn(appMetadata).when(auth0Template).getUserAppMetadata(anyString());
        doReturn(mockUsers).when(auth0Template).usersByEmailSortByCreatedAt(anyString());
        Map<String, String> externalProperty = Maps.newHashMap();
        String choosePlan = "true";
        externalProperty.put("choosePlan", choosePlan);
        Auth0User auth0User =
            auth0Service.createUserByAuth0IfNotExist(auth0UserProfile, externalProperty);
        assertThat(auth0User).isNotNull();
        assertThat(auth0User.getUserId()).isNotNull();
        assertThat(auth0User.getQueryString()).isNotEmpty()
            .contains("choosePlan=" + choosePlan,
                "template=ai_onboarding");
    }

    @Test
    void testCreateUserByAuth0IfNotExistWithTwoUser() {
        Auth0UserProfile auth0UserProfile = new Auth0UserProfile();
        auth0UserProfile.setNickname("auth0 user");
        auth0UserProfile.setEmail("auth0@test.com");
        auth0UserProfile.setSub("google-oauth2|64f957b0827779ba96efcbcd33");

        Map<String, Object> appMetadata = new HashMap<>(1);
        appMetadata.put("template", "ai_onboarding");
        User user = new User();
        user.setAppMetadata(appMetadata);
        List<User> mockUsers = createMockUsers(2);

        doReturn(appMetadata).when(auth0Template).getUserAppMetadata(anyString());
        doReturn(mockUsers).when(auth0Template).usersByEmailSortByCreatedAt(anyString());
        Auth0User auth0User =
            auth0Service.createUserByAuth0IfNotExist(auth0UserProfile, Maps.newHashMap());
        assertThat(auth0User).isNotNull();
        assertThat(auth0User.getUserId()).isNotNull();
        assertThat(auth0User.getQueryString()).isNotEmpty();
    }

    @Test
    void testCreateUserByAuth0IfNotExistWithNoOne() {
        Auth0UserProfile auth0UserProfile = new Auth0UserProfile();
        auth0UserProfile.setNickname("auth0 user");
        auth0UserProfile.setEmail("auth0@test.com");
        auth0UserProfile.setSub("google-oauth2|" + UUID.randomUUID());

        Map<String, Object> appMetadata = new HashMap<>(1);
        appMetadata.put("template", "ai_onboarding");
        User user = new User();
        user.setAppMetadata(appMetadata);
        List<User> mockUsers = new ArrayList<>();

        doReturn(appMetadata).when(auth0Template).getUserAppMetadata(anyString());
        doReturn(mockUsers).when(auth0Template).usersByEmailSortByCreatedAt(anyString());
        try {
            auth0Service.createUserByAuth0IfNotExist(auth0UserProfile, Maps.newHashMap());
        } catch (BusinessException e) {
            // When there is no user found in auth 0, a custom exception is thrown, which is normal
        }
    }

    // creating simulated user lists
    private static List<User> createMockUsers(int count) {
        List<User> mockUsers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setNickname("auth0 user");
            user.setEmail("auth0@test.com");
            user.setId("google-oauth2|" + UUID.randomUUID());
            mockUsers.add(user);
        }
        return mockUsers;
    }

}
