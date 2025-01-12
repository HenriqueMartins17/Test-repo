package com.apitable.enterprise.apitablebilling.appsumo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.apitablebilling.interfaces.facade.EnterpriseEntitlementServiceFacadeImpl;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.UserException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppsumoServiceImplTest extends AbstractApitableSaasIntegrationTest {
    @Autowired
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Autowired
    private IAppsumoService iAppsumoService;

    @Test
    void testGenerateJwtTokenWithWrongUser() {
        assertThrows(IllegalStateException.class,
            () -> iAppsumoService.getAccessToken("test", "test"));
    }

    @Test
    void testGenerateJwtTokenWith() {
        String token = iAppsumoService.getAccessToken("test", "test_secret");
        assertThat(token).isNotNull();
    }

    @Test
    void testVerifyToken() {
        String token = iAppsumoService.getAccessToken("test", "test_secret");
        boolean result = iAppsumoService.verifyAccessToken("Bearer " + token);
        assertThat(result).isTrue();
    }

    @Test
    void testUserSignUpAlreadyExists() throws Auth0Exception {
        String email = "test@aitable.ai";
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier1",
                "test", email, "test_invoice_id");
        when(auth0Template.usersByEmail(email)).thenReturn(new User());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iAppsumoService.userSignup(eventId, "password"));
        assertThat(exception.getCode()).isEqualTo(UserException.REGISTER_EMAIL_HAS_EXIST.getCode());
    }

    @Test
    void testUserSignUp() throws Auth0Exception {
        String email = "testccc@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("testccc");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier1",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        assertThat(userId).isNotNull();
    }

    @Test
    void testUserIsMainAdmin() throws Auth0Exception {
        String email = "test_admin@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test_admin");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier1",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");

        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        assertThat(spaceIds).isNotEmpty();
    }

    @Test
    void testUserSpaceSubscription() throws Auth0Exception {
        String email = "test_tier2@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test_tier2");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier1",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        EnterpriseEntitlementServiceFacadeImpl entitlementServiceFacade =
            new EnterpriseEntitlementServiceFacadeImpl(iEntitlementService);
        SubscriptionInfo subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscription(spaceIds.get(0));
        assertThat(subscriptionInfo).isNotNull();
    }

    @Test
    void testUserSpacesSubscription() throws Auth0Exception {
        String email = "test_02@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test_02");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier1",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        EnterpriseEntitlementServiceFacadeImpl entitlementServiceFacade =
            new EnterpriseEntitlementServiceFacadeImpl(iEntitlementService);
        Map<String, SubscriptionFeature> subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscriptions(spaceIds);
        assertThat(subscriptionInfo).isNotNull();
    }

    @Test
    void testUserSpaceSubscriptionCapacity() throws Auth0Exception {
        String email = "test001@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test001");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        EnterpriseEntitlementServiceFacadeImpl entitlementServiceFacade =
            new EnterpriseEntitlementServiceFacadeImpl(iEntitlementService);
        SubscriptionInfo subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscription(spaceIds.get(0));
        assertThat(subscriptionInfo.getFeature().getCapacitySize().getValue()).isNotNull();
    }

    @Test
    void testUserSpaceApiCall() throws Auth0Exception {
        String email = "test0001@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test0001");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier4",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        EnterpriseEntitlementServiceFacadeImpl entitlementServiceFacade =
            new EnterpriseEntitlementServiceFacadeImpl(iEntitlementService);
        SubscriptionInfo subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscription(spaceIds.get(0));
        assertThat(subscriptionInfo.getFeature().getApiCallNumsPerMonth().getValue()).isNotNull();
    }

    @Test
    void testUserSpaceSeats() throws Auth0Exception {
        String email = "test0002@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test12");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier4",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        EnterpriseEntitlementServiceFacadeImpl entitlementServiceFacade =
            new EnterpriseEntitlementServiceFacadeImpl(iEntitlementService);
        SubscriptionInfo subscriptionInfo =
            entitlementServiceFacade.getSpaceSubscription(spaceIds.get(0));
        assertThat(subscriptionInfo.getFeature().getSeat().getValue()).isNotNull();
    }

    @Test
    void testHandleUpgrade() throws Auth0Exception {
        String email = "test12@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test12");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier4",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");

        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ENHANCE_TIER.getAction(), "aitable_tier5",
                "test",
                email, null);
        iAppsumoService.handleEvent(logId, AppsumoAction.ENHANCE_TIER);

        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(logId);
        assertThat(event.getHandleStatus()).isEqualTo(AppsumoHandleStatus.SUCCESS.getStatus());
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier5");

    }

    @Test
    void testHandleUpgradeWhenNotActive() {
        iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier4",
            "test", "test_email", "test_invoice_id");
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ENHANCE_TIER.getAction(), "aitable_tier5",
                "test", "test_email", null);
        iAppsumoService.handleEvent(logId, AppsumoAction.ENHANCE_TIER);

        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(logId);
        assertThat(event.getHandleStatus()).isEqualTo(AppsumoHandleStatus.HANDLING.getStatus());
    }

    @Test
    void testHandleDowngrade() throws Auth0Exception {
        String email = "test013@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        User user = new User();
        user.setId(email);
        user.setEmail(email);
        user.setNickname("test013");
        user.setPassword("password".toCharArray());
        user.setPicture("");
        when(auth0Template.createUser(email, "password", false)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier3",
                "test", email, "test_invoice_id");
        Long userId = iAppsumoService.userSignup(eventId, "password");
        List<String> spaceIds = iMemberService.getUserOwnSpaceIds(userId);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.REDUCE_TIER.getAction(), "aitable_tier1",
                "test",
                email, null);
        iAppsumoService.handleEvent(logId, AppsumoAction.REDUCE_TIER);

        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(logId);
        assertThat(event.getHandleStatus()).isEqualTo(AppsumoHandleStatus.SUCCESS.getStatus());

        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier1");
    }

    @Test
    void testHandleRefund() throws Auth0Exception {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String email = iUserService.getEmailByUserId(userSpace.getUserId());
        User user = new User();
        user.setId("test_id");
        iUserBindService.create(userSpace.getUserId(), user.getId());
        when(auth0Template.usersByEmail(email)).thenReturn(user);
        Long eventId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test", email, "test_invoice_id");
        iAppsumoService.handleEvent(eventId, AppsumoAction.ACTIVATE);
        Long refundLogId =
            iAppsumoEventLogService.create(AppsumoAction.REFUND.getAction(), "aitable_tier2",
                "test",
                email, null);
        iAppsumoService.handleEvent(refundLogId, AppsumoAction.REFUND);
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(refundLogId);
        assertThat(event.getHandleStatus()).isEqualTo(AppsumoHandleStatus.SUCCESS.getStatus());
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo.isFree()).isTrue();
    }

    @Test
    void testActiveEmailNotExistInAuth0() throws Auth0Exception {
        String email = "test017@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                email, "test_invoice_id");
        EventVO result = iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE);
        assertThat(result.getRedirectUrl()).contains("/user/appsumo?state=");
    }

    @Test
    void testActiveEmailExistInAuth0ButNotInLocalDB() throws Auth0Exception {
        String email = "test017@aitable.ai";
        User user = new User();
        user.setId("test017");
        when(auth0Template.usersByEmail(email)).thenReturn(user);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                email, "test_invoice_id");
        BusinessException exception =
            Assertions.assertThrows(BusinessException.class,
                () -> iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE));
        assertEquals(1705, exception.getCode());
    }


    @Test
    void testActiveEmailExistInAuth0AndLocalDBNoSpace() throws Auth0Exception {
        String email = "test017@aitable.ai";
        UserEntity localUser = iUserService.createUserByEmail(email, "123456");
        iUserBindService.create(localUser.getId(), "externalKey");
        User user = new User();
        user.setId("externalKey");
        when(auth0Template.usersByEmail(email)).thenReturn(user);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                email, "test_invoice_id");
        EventVO result = iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE);
        assertThat(result.getRedirectUrl()).contains("/workbench");
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(localUser.getId());
        assertThat(spaceIds.size()).isEqualTo(1);
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier2");
    }

    @Test
    void testActiveEmailExistInAuth0AndLocalDBWithOnlyOneSpaceAndFree() throws Auth0Exception {
        // should not create space
        MockUserSpace mockUser = createSingleUserAndSpace();
        UserEntity localUser = iUserService.getById(mockUser.getUserId());
        iUserBindService.create(localUser.getId(), "externalKey");
        User user = new User();
        user.setId("externalKey");
        when(auth0Template.usersByEmail(localUser.getEmail())).thenReturn(user);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier3",
                "test",
                localUser.getEmail(), "test_invoice_id");
        EventVO result = iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE);
        assertThat(result.getRedirectUrl()).contains("/workbench");
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(localUser.getId());
        assertThat(spaceIds.size()).isEqualTo(1);
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(mockUser.getSpaceId());
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier3");
    }

    @Test
    void testActiveEmailExistInAuth0AndLocalDBWithOnlyOneSpaceAndNotFree() throws Auth0Exception {
        // should create a new space
        MockUserSpace mockUser = createSingleUserAndSpace();
        UserEntity localUser = iUserService.getById(mockUser.getUserId());
        iUserBindService.create(localUser.getId(), "externalKey");
        User user = new User();
        user.setId("externalKey");
        when(auth0Template.usersByEmail(localUser.getEmail())).thenReturn(user);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                localUser.getEmail(), "test_invoice_id");
        iAppsumoService.createSubscription(localUser.getId(), mockUser.getSpaceId(),
            iAppsumoEventLogService.getSimpleInfoById(logId));
        getClock().addDays(1);
        EventVO result = iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE);
        assertThat(result.getRedirectUrl()).contains("/workbench");
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(localUser.getId());
        assertThat(spaceIds.size()).isEqualTo(2);
        spaceIds = spaceIds.stream().filter(i -> !i.equals(mockUser.getSpaceId())).toList();
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier2");
    }

    @Test
    void testActiveEmailExistInAuth0AndLocalDBWithTwoSpace() throws Auth0Exception {
        // should create a new space
        MockUserSpace mockUser = createSingleUserAndSpace();
        UserEntity localUser = iUserService.getById(mockUser.getUserId());
        String spaceId = createSpaceWithoutName(localUser);
        iUserBindService.create(localUser.getId(), "externalKey");
        User user = new User();
        user.setId("externalKey");
        when(auth0Template.usersByEmail(localUser.getEmail())).thenReturn(user);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                localUser.getEmail(), "test_invoice_id");
        EventVO result = iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE);
        assertThat(result.getRedirectUrl()).contains("/workbench");
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(localUser.getId());
        assertThat(spaceIds.size()).isEqualTo(3);
        spaceIds =
            spaceIds.stream().filter(i -> !i.equals(spaceId) && !i.equals(mockUser.getSpaceId()))
                .toList();
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
        assertThat(subscriptionInfo.getProduct()).isEqualTo("appsumo_tier2");
    }

    @Test
    void testActiveUserWhenGetUserError() throws Auth0Exception {
        String email = "test017@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenThrow(new Auth0Exception("user not found"));
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                email, "test_invoice_id");
        BusinessException exception =
            Assertions.assertThrows(BusinessException.class,
                () -> iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE));
        assertEquals(1704, exception.getCode());
    }

    @Test
    void testActiveUserWhenUserIsNull() throws Auth0Exception {
        String email = "test017@aitable.ai";
        when(auth0Template.usersByEmail(email)).thenReturn(null);
        Long logId =
            iAppsumoEventLogService.create(AppsumoAction.ACTIVATE.getAction(), "aitable_tier2",
                "test",
                email, "test_invoice_id");
        assertDoesNotThrow(() -> iAppsumoService.handleEvent(logId, AppsumoAction.ACTIVATE));
    }

}
