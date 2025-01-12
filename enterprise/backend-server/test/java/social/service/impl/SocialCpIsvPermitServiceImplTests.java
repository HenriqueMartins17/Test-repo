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

package com.apitable.enterprise.social.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.apitable.core.util.DateTimeUtil;
import com.apitable.enterprise.AbstractIsvTest;
import com.apitable.enterprise.social.autoconfigure.wecom.WeComProperties;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderAccountEntity;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialWecomPermitDelayService;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderAccountService;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderService;
import com.apitable.enterprise.social.service.IsocialWecomPermitOrderAccountBindService;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.WxCpIsvPermitServiceImpl;
import com.vikadata.social.wecom.WxCpIsvServiceImpl;
import com.vikadata.social.wecom.model.WxCpIsvPermitBatchGetActiveInfo;
import com.vikadata.social.wecom.model.WxCpIsvPermitCreateNewOrder;
import com.vikadata.social.wecom.model.WxCpIsvPermitCreateRenewOrderRequest;
import com.vikadata.social.wecom.model.WxCpIsvPermitCreateRenewOrderResponse;
import com.vikadata.social.wecom.model.WxCpIsvPermitGetOrder;
import com.vikadata.social.wecom.model.WxCpIsvPermitListOrderAccount;
import com.vikadata.social.wecom.model.WxCpIsvPermitSubmitRenewOrder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * <p>
 * WeCom Service Provider Interface License
 * </p>
 */
@Disabled
class SocialCpIsvPermitServiceImplTests extends AbstractIsvTest {

    @MockBean
    protected ISocialCpIsvPermitService socialCpIsvPermitService;

    @MockBean
    protected WeComProperties weComProperties;

    @MockBean
    protected WeComTemplate weComTemplate;

    @MockBean
    protected IBundleService bundleService;

    @MockBean
    protected ISocialWecomPermitDelayService socialWecomPermitDelayService;

    @MockBean
    protected ISocialWecomPermitOrderService socialWecomPermitOrderService;

    @MockBean
    protected ISocialWecomPermitOrderAccountService socialWecomPermitOrderAccountService;

    @MockBean
    protected IsocialWecomPermitOrderAccountBindService socialWecomPermitOrderAccountBindService;

    @MockBean
    protected ISocialTenantService iSocialTenantService;

    @MockBean
    protected ISocialTenantBindService socialTenantBindService;

    @Test
    void createNewOrderTest() throws Exception {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String spaceId = "testSpaceId";
        String cpUserId = "testCpUserId";
        String permitBuyerUserId = "testPermitBuyerUserId";
        String orderId = "testOrderId";

        mockGetBySpaceId(suiteId, authCorpId, spaceId);
        mockGetNeedActivateCpUserIds(cpUserId);
        mockWxCpIsvPermitService();
        mockGetIsvAppList(suiteId, permitBuyerUserId);
        mockCreateNewOrder(orderId);
        mockGetOrder(authCorpId, orderId, false);
        mockSave(suiteId, authCorpId, orderId, permitBuyerUserId);

        SocialWecomPermitOrderEntity orderEntity =
            socialCpIsvPermitService.createNewOrder(spaceId, 12);

        Assertions.assertNotNull(orderEntity);
    }

    @Test
    void activateOrderTest() throws Exception {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String spaceId = "testSpaceId";
        String activeCode = "testActiveCode";
        String orderId = "testOrderId";

        mockGetByOrderId(suiteId, authCorpId, orderId);
        mockWxCpIsvPermitService();
        mockGetOrder(authCorpId, orderId, true);
        mockUpdateById();
        mockGetTenantBindSpaceId(spaceId);
        mockGetCountByOrderId();
        mockSaveAllActiveCodes(suiteId, authCorpId, orderId, activeCode);

        socialCpIsvPermitService.activateOrder(orderId);

        List<String> activeCodes =
            realSocialWecomPermitOrderAccountService.getActiveCodesByOrderId(suiteId, authCorpId,
                orderId, null);
        Assertions.assertEquals(1, activeCodes.size());
    }

    @Test
    void renewalCpUser() throws Exception {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String spaceId = "testSpaceId";
        String cpUserId = "testCpUserId";
        String permitBuyerUserId = "testPermitBuyerUserId";
        String orderId = "testOrderId";
        String jobId = "testJobId";

        mockGetBySpaceId(suiteId, authCorpId, spaceId);
        mockWxCpIsvPermitService();
        mockCreateRenewOrder(jobId);
        mockGetIsvAppList(suiteId, permitBuyerUserId);
        mockSubmitRenewOrder(orderId);
        mockGetOrder(authCorpId, orderId, false);
        mockSave(suiteId, authCorpId, orderId, permitBuyerUserId);

        SocialWecomPermitOrderEntity orderEntity =
            socialCpIsvPermitService.renewalCpUser(spaceId, Collections.singletonList(cpUserId),
                12);

        assertThat(orderEntity).isNotNull();
    }

    @Test
    void ensureAllTest() throws Exception {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String activeCode = "testActiveCode";
        String orderId = "testOrderId";

        // Pre populated data
        WxCpIsvPermitListOrderAccount.AccountList accountList =
            new WxCpIsvPermitListOrderAccount.AccountList();
        accountList.setActiveCode(activeCode);
        accountList.setType(1);
        realSocialWecomPermitOrderAccountService
            .batchSaveActiveCode(suiteId, activeCode, orderId,
                Collections.singletonList(accountList));

        mockGetByOrderId(suiteId, authCorpId, orderId);
        mockWxCpIsvPermitService();
        mockGetOrder(authCorpId, orderId, true);
        mockUpdateById();
        mockGetActiveCodes(activeCode);
        mockEnsureActiveCodes(suiteId, authCorpId, activeCode);

        socialCpIsvPermitService.ensureOrderAndAllActiveCodes(orderId);

        List<String> activeCodes =
            realSocialWecomPermitOrderAccountService.getActiveCodesByOrderId(suiteId, authCorpId,
                orderId, null);
        Assertions.assertEquals(1, activeCodes.size());
    }

    @Test
    void calcNewAccountCountTest() {
        String suiteId = "suiteId";
        String authCorpId = "authCorpId";
        String spaceId = createWecomIsvTenant(suiteId, authCorpId);
        int newAccountCount =
            realSocialCpIsvPermitService.calcNewAccountCount(suiteId, authCorpId, spaceId);
        Assertions.assertEquals(0, newAccountCount);
    }

    @Test
    void ensureOrderTest() throws WxErrorException {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String orderId = "testOrderId";

        mockGetByOrderId(suiteId, authCorpId, orderId);
        mockWxCpIsvPermitService();
        mockGetOrder(authCorpId, orderId, true);
        mockUpdateById();

        SocialWecomPermitOrderEntity orderEntity = socialCpIsvPermitService.ensureOrder(orderId);
        Assertions.assertNotNull(orderEntity);
    }

    @Test
    void ensureAllActiveCodesTest() throws WxErrorException {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String activeCode = "testActiveCode";
        String orderId = "testOrderId";
        // Pre populated data
        WxCpIsvPermitListOrderAccount.AccountList accountList =
            new WxCpIsvPermitListOrderAccount.AccountList();
        accountList.setActiveCode(activeCode);
        accountList.setType(1);
        realSocialWecomPermitOrderAccountService
            .batchSaveActiveCode(suiteId, activeCode, orderId,
                Collections.singletonList(accountList));
        mockGetActiveCodes(activeCode);
        mockEnsureActiveCodes(suiteId, authCorpId, activeCode);
        socialCpIsvPermitService.ensureAllActiveCodes(suiteId, authCorpId);
        List<String> activeCodes =
            realSocialWecomPermitOrderAccountService.getActiveCodesByOrderId(suiteId, authCorpId,
                orderId, null);
        Assertions.assertEquals(1, activeCodes.size());
    }

    @Test
    void autoProcessPermitOrderTest() {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String spaceId = createWecomIsvTenant(suiteId, authCorpId);
        String permitBuyerUserId = "testPermitBuyerUserId";
        Bundle bundle = realGetActivatedBundleBySpaceId(spaceId);
        if (Objects.nonNull(bundle)) {
            realGetByAppIdAndTenantId(suiteId, authCorpId);
            mockGetIsvAppList(suiteId, permitBuyerUserId);
            mockAddAuthCorp();
        }
        socialCpIsvPermitService.autoProcessPermitOrder(suiteId, authCorpId, spaceId);
        List<String> activeCodes =
            realSocialWecomPermitOrderAccountService.getActiveCodes(suiteId, authCorpId, null);
        Assertions.assertEquals(0, activeCodes.size());
    }

    @Test
    void createPermitOrderTest() {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String spaceId = createWecomIsvTenant(suiteId, authCorpId);
        LocalDateTime expireTime = DateTimeUtil.localDateTimeFromNow(8, 15, 0, 0, 0);
        boolean result =
            socialCpIsvPermitService.createPermitOrder(suiteId, authCorpId, spaceId, expireTime);
        Assertions.assertFalse(result);
    }

    @Test
    void sendNewWebhookTest() {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String orderId = "testOrderId";
        String permitBuyerUserId = "testPermitBuyerUserId";
        mockGetIsvAppList(suiteId, permitBuyerUserId);
        mockPostForObject();
        boolean result =
            socialCpIsvPermitService.sendNewWebhook(suiteId, authCorpId, null, orderId, null);
        Assertions.assertTrue(result);
    }

    @Test
    void sendRenewWebhookTest() {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String orderId = "testOrderId";
        String permitBuyerUserId = "testPermitBuyerUserId";
        mockGetIsvAppList(suiteId, permitBuyerUserId);
        mockPostForObject();
        boolean result =
            socialCpIsvPermitService.sendRenewWebhook(suiteId, authCorpId, null, orderId);
        Assertions.assertTrue(result);
    }

    @Test
    void sendRefundWebhookTest() {
        String suiteId = "testSuiteId";
        String authCorpId = "testAuthCorpId";
        String permitBuyerUserId = "testPermitBuyerUserId";
        mockGetIsvAppList(suiteId, permitBuyerUserId);
        mockPostForObject();
        boolean result = socialCpIsvPermitService.sendRefundWebhook(suiteId, authCorpId);
        Assertions.assertTrue(result);
    }

    /**
     * Mock {@link ISocialTenantBindService#getBySpaceId(String)}
     *
     * @param suiteId    Expected App Suite ID
     * @param authCorpId Expected Authorized Enterprise ID
     * @param spaceId    Expected space ID
     */
    private void mockGetBySpaceId(String suiteId, String authCorpId, String spaceId) {
        Mockito.when(socialTenantBindService.getBySpaceId(spaceId))
            .thenReturn(SocialTenantBindEntity.builder()
                .appId(suiteId)
                .tenantId(authCorpId)
                .spaceId(spaceId)
                .build());
    }

    /**
     * Mock {@link ISocialWecomPermitOrderAccountService#getNeedActivateCpUserIds(String, String, String)}
     *
     * @param cpUserId Expected enterprise WeCom users ID
     */
    private void mockGetNeedActivateCpUserIds(String cpUserId) {
        Mockito.when(socialWecomPermitOrderAccountService.getNeedActivateCpUserIds(Mockito.any(),
                Mockito.any(), Mockito.any()))
            .thenReturn(Collections.singletonList(cpUserId));
    }

    /**
     * Mock Get {@link WxCpIsvPermitServiceImpl}
     */
    private void mockWxCpIsvPermitService() {
        WxCpIsvServiceImpl wxCpIsvService = Mockito.mock(WxCpIsvServiceImpl.class);
        Mockito.when(weComTemplate.isvService(Mockito.any()))
            .thenReturn(wxCpIsvService);
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(wxCpIsvService.getWxCpIsvPermitService())
            .thenReturn(wxCpIsvPermitService);
    }

    /**
     * Mock Get enterprise WeCom application information configuration
     *
     * @param suiteId           Expected App Suite ID
     * @param permitBuyerUserId The enterprise WeCom user ID of the person under the expected interface license
     */
    private void mockGetIsvAppList(String suiteId, String permitBuyerUserId) {
        WeComProperties.IsvApp isvApp = new WeComProperties.IsvApp();
        isvApp.setSuiteId(suiteId);
        isvApp.setPermitBuyerUserId(permitBuyerUserId);
        isvApp.setPermitNotifyWebhookUrl("webhook url");
        isvApp.setPermitNotifyWebhookSecret("webhook secret");
        Mockito.when(weComProperties.getIsvAppList())
            .thenReturn(Collections.singletonList(isvApp));
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#createNewOrder(String, Integer, Integer, String)}
     *
     * @param orderId Expected interface license number
     */
    private void mockCreateNewOrder(String orderId) throws WxErrorException {
        WxCpIsvPermitCreateNewOrder wxCpIsvPermitCreateNewOrder = new WxCpIsvPermitCreateNewOrder();
        wxCpIsvPermitCreateNewOrder.setOrderId(orderId);
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(
                wxCpIsvPermitService.createNewOrder(Mockito.any(), Mockito.any(), Mockito.any(),
                    Mockito.any()))
            .thenReturn(wxCpIsvPermitCreateNewOrder);
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#getOrder(String)}
     *
     * @param authCorpId Expected Authorized Enterprise ID
     * @param orderId    Expected interface license number
     * @param isPaid     Whether it is expected to have been paid
     */
    private void mockGetOrder(String authCorpId, String orderId, boolean isPaid)
        throws WxErrorException {
        WxCpIsvPermitGetOrder wxCpIsvPermitGetOrder = new WxCpIsvPermitGetOrder();
        WxCpIsvPermitGetOrder.Order wxCpIsvPermitGetOrderDetail = new WxCpIsvPermitGetOrder.Order();
        wxCpIsvPermitGetOrderDetail.setOrderId(orderId);
        wxCpIsvPermitGetOrderDetail.setOrderType(1);
        wxCpIsvPermitGetOrderDetail.setOrderStatus(isPaid ? 1 : 0);
        wxCpIsvPermitGetOrderDetail.setCorpId(authCorpId);
        wxCpIsvPermitGetOrderDetail.setPrice(100);
        WxCpIsvPermitGetOrder.Order.AccountCount accountCount =
            new WxCpIsvPermitGetOrder.Order.AccountCount();
        accountCount.setBaseCount(1);
        accountCount.setExternalContactCount(0);
        wxCpIsvPermitGetOrderDetail.setAccountCount(accountCount);
        WxCpIsvPermitGetOrder.Order.AccountDuration accountDuration =
            new WxCpIsvPermitGetOrder.Order.AccountDuration();
        accountDuration.setMonths(12);
        wxCpIsvPermitGetOrderDetail.setAccountDuration(accountDuration);
        wxCpIsvPermitGetOrderDetail.setCreateTime(
            LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)));
        if (isPaid) {
            wxCpIsvPermitGetOrderDetail.setPayTime(
                LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)));
        }
        wxCpIsvPermitGetOrder.setOrder(wxCpIsvPermitGetOrderDetail);
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(wxCpIsvPermitService.getOrder(Mockito.any()))
            .thenReturn(wxCpIsvPermitGetOrder);
    }

    /**
     * Mock {@link ISocialWecomPermitOrderService#save(Object)}
     */
    private void mockSave(String suiteId, String authCorpId, String orderId, String buyerUserId) {
        SocialWecomPermitOrderEntity socialWecomPermitOrderEntity =
            SocialWecomPermitOrderEntity.builder()
                .suiteId(suiteId)
                .authCorpId(authCorpId)
                .orderId(orderId)
                .orderType(1)
                .orderStatus(0)
                .price(100)
                .baseAccountCount(1)
                .externalAccountCount(0)
                .durationMonths(12)
                .createTime(DateTimeUtil.localDateTimeNow(8))
                .payTime(null)
                .buyerUserId(buyerUserId)
                .build();
        Mockito.when(socialWecomPermitOrderService.save(Mockito.any()))
            .thenAnswer(
                invocation -> realSocialWecomPermitOrderService.save(socialWecomPermitOrderEntity));
    }

    /**
     * Mock {@link ISocialWecomPermitOrderService#getByOrderId(String)}
     *
     * @param suiteId    Expected App Suite ID
     * @param authCorpId Expected Authorized Enterprise ID
     * @param orderId    Expected interface license number
     */
    private void mockGetByOrderId(String suiteId, String authCorpId, String orderId) {
        Mockito.when(socialWecomPermitOrderService.getByOrderId(Mockito.any()))
            .thenReturn(SocialWecomPermitOrderEntity.builder()
                .suiteId(suiteId)
                .authCorpId(authCorpId)
                .orderId(orderId)
                .orderType(1)
                .orderStatus(0)
                .price(100)
                .baseAccountCount(1)
                .externalAccountCount(0)
                .durationMonths(12)
                .createTime(DateTimeUtil.localDateTimeNow(8))
                .payTime(null)
                .build());
    }

    /**
     * Mock {@link ISocialWecomPermitOrderService#updateById(Object)}
     */
    private void mockUpdateById() {
        Mockito.when(socialWecomPermitOrderService.updateById(Mockito.any()))
            .thenReturn(true);
    }

    /**
     * Mock {@link ISocialTenantBindService#getTenantBindSpaceId(String, String)}
     *
     * @param spaceId Expected space ID
     */
    private void mockGetTenantBindSpaceId(String spaceId) {
        Mockito.when(socialTenantBindService.getTenantBindSpaceId(Mockito.any(), Mockito.any()))
            .thenReturn(spaceId);
    }

    /**
     * Mock {@link IsocialWecomPermitOrderAccountBindService#getCountByOrderId(String)}
     */
    private void mockGetCountByOrderId() {
        Mockito.when(socialWecomPermitOrderAccountBindService.getCountByOrderId(Mockito.any()))
            .thenReturn(0);
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#listOrderAccount(String, Integer, String)}
     *
     * @param activeCode Expected return activation code
     */
    private void mockListOrderAccount(String activeCode) throws WxErrorException {
        WxCpIsvPermitListOrderAccount wxCpIsvPermitListOrderAccount =
            new WxCpIsvPermitListOrderAccount();
        wxCpIsvPermitListOrderAccount.setHasMore(0);
        WxCpIsvPermitListOrderAccount.AccountList accountList =
            new WxCpIsvPermitListOrderAccount.AccountList();
        accountList.setActiveCode(activeCode);
        accountList.setType(1);
        wxCpIsvPermitListOrderAccount.setAccountList(Collections.singletonList(accountList));
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(
                wxCpIsvPermitService.listOrderAccount(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(wxCpIsvPermitListOrderAccount);
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#batchGetActiveInfo(String, List)}
     *
     * @param activeCode Expected return activation code
     */
    private void mockBatchGetActiveInfo(String activeCode) throws WxErrorException {
        WxCpIsvPermitBatchGetActiveInfo.ActiveInfoList activeInfoList =
            new WxCpIsvPermitBatchGetActiveInfo.ActiveInfoList();
        activeInfoList.setActiveCode(activeCode);
        activeInfoList.setType(1);
        activeInfoList.setStatus(1);
        activeInfoList.setCreateTime(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)));
        WxCpIsvPermitBatchGetActiveInfo wxCpIsvPermitBatchGetActiveInfo =
            new WxCpIsvPermitBatchGetActiveInfo();
        wxCpIsvPermitBatchGetActiveInfo.setActiveInfoList(
            Collections.singletonList(activeInfoList));
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(wxCpIsvPermitService.batchGetActiveInfo(Mockito.any(), Mockito.any()))
            .thenReturn(wxCpIsvPermitBatchGetActiveInfo);
    }

    /**
     * Mock {@link ISocialWecomPermitOrderAccountService#getByActiveCodes(String, String, List)}
     *
     * @param suiteId    Expected returned application suite ID
     * @param authCorpId Expected returned authorized enterprise ID
     * @param activeCode Expected return activation code
     */
    private void mockGetByActiveCodes(String suiteId, String authCorpId, String activeCode) {
        Mockito.when(
                socialWecomPermitOrderAccountService.getByActiveCodes(Mockito.any(), Mockito.any(),
                    Mockito.any()))
            .thenReturn(Collections.singletonList(SocialWecomPermitOrderAccountEntity.builder()
                .suiteId(suiteId)
                .authCorpId(authCorpId)
                .type(1)
                .activateStatus(1)
                .activeCode(activeCode)
                .build()));
    }

    /**
     * Mock {@link ISocialWecomPermitOrderAccountService#updateBatchById(Collection)}
     */
    private void mockUpdateBatchById() {
        Mockito.when(socialWecomPermitOrderAccountService.updateBatchById(Mockito.any()))
            .thenReturn(true);
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#createRenewOrder(WxCpIsvPermitCreateRenewOrderRequest)}
     *
     * @param jobId Expected returned jobId
     */
    private void mockCreateRenewOrder(String jobId) throws WxErrorException {
        WxCpIsvPermitCreateRenewOrderResponse wxCpIsvPermitCreateRenewOrderResponse =
            new WxCpIsvPermitCreateRenewOrderResponse();
        wxCpIsvPermitCreateRenewOrderResponse.setJobId(jobId);
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(wxCpIsvPermitService.createRenewOrder(Mockito.any()))
            .thenReturn(wxCpIsvPermitCreateRenewOrderResponse);
    }

    /**
     * Mock {@link WxCpIsvPermitServiceImpl#submitRenewOrder(String, Integer, String)}
     *
     * @param orderId Expected return order number
     */
    private void mockSubmitRenewOrder(String orderId) throws WxErrorException {
        WxCpIsvPermitSubmitRenewOrder wxCpIsvPermitSubmitRenewOrder =
            new WxCpIsvPermitSubmitRenewOrder();
        wxCpIsvPermitSubmitRenewOrder.setOrderId(orderId);
        WxCpIsvPermitServiceImpl wxCpIsvPermitService =
            Mockito.mock(WxCpIsvPermitServiceImpl.class);
        Mockito.when(
                wxCpIsvPermitService.submitRenewOrder(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(wxCpIsvPermitSubmitRenewOrder);
    }

    /**
     * Mock {@link ISocialWecomPermitOrderAccountService#getActiveCodes(String, String, List)}
     *
     * @param activeCode Expected return activation code
     */
    private void mockGetActiveCodes(String activeCode) {
        Mockito.when(
                socialWecomPermitOrderAccountService.getActiveCodes(Mockito.any(), Mockito.any(),
                    Mockito.any()))
            .thenReturn(Collections.singletonList(activeCode));
    }

    /**
     * Mock {@link ISocialWecomPermitDelayService#addAuthCorp(String, String, LocalDateTime, Integer, Integer)}
     */
    private void mockAddAuthCorp() {
        lenient().when(
                socialWecomPermitDelayService.addAuthCorp(Mockito.any(), Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any()))
            .thenReturn(null);
    }

    private void mockPostForObject() {
        when(
            restClient
                .post()
                .uri(anyString())
                .body(any())
                .retrieve()
                .body(String.class)
        ).thenReturn("{\"StatusCode\":0}");
    }

    /**
     * Mock Save Activation Code
     *
     * @param activeCode Expected return activation code
     */
    private void mockSaveAllActiveCodes(String suite, String authCorpId, String orderId,
                                        String activeCode) throws WxErrorException {
        mockWxCpIsvPermitService();
        mockListOrderAccount(activeCode);
        realBatchSaveActiveCode(suite, authCorpId, orderId, activeCode);
    }

    /**
     * Mock Confirm Activation Code
     *
     * @param suiteId    Expected App Suite ID
     * @param authCorpId Expected Authorized Enterprise ID
     * @param activeCode Expected activation code
     */
    private void mockEnsureActiveCodes(String suiteId, String authCorpId, String activeCode)
        throws WxErrorException {
        mockWxCpIsvPermitService();
        mockBatchGetActiveInfo(activeCode);
        mockGetByActiveCodes(suiteId, authCorpId, activeCode);
        mockUpdateBatchById();
    }

    /**
     * Call {@link ISocialWecomPermitOrderAccountService#batchSaveActiveCode(String, String, String, List)}
     *
     * @param suiteId    Pre application Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param orderId    order ID
     * @param activeCode Activation code
     */
    private void realBatchSaveActiveCode(String suiteId, String authCorpId, String orderId,
                                         String activeCode) {
        WxCpIsvPermitListOrderAccount.AccountList accountList =
            new WxCpIsvPermitListOrderAccount.AccountList();
        accountList.setActiveCode(activeCode);
        accountList.setType(1);

        Mockito.doAnswer(invocation -> {
                realSocialWecomPermitOrderAccountService
                    .batchSaveActiveCode(suiteId, authCorpId, orderId,
                        Collections.singletonList(accountList));
                return null;
            })
            .when(socialWecomPermitOrderAccountService)
            .batchSaveActiveCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * Call {@link IBundleService#getActivatedBundleBySpaceId(String)}
     *
     * @param spaceId Space ID
     */
    private Bundle realGetActivatedBundleBySpaceId(String spaceId) {
        return Mockito.doAnswer(
                invocation -> iBundleService.getActivatedBundleBySpaceId(spaceId))
            .when(bundleService)
            .getActivatedBundleBySpaceId(spaceId);
    }

    /**
     * Call {@link ISocialTenantService#getByAppIdAndTenantId(String, String)}
     *
     * @param suiteId    Pre application Suite ID
     * @param authCorpId Authorized enterprise ID
     */
    private void realGetByAppIdAndTenantId(String suiteId, String authCorpId) {
        Mockito.doAnswer(
                invocation -> iSocialTenantService.getByAppIdAndTenantId(suiteId, authCorpId))
            .when(iSocialTenantService)
            .getByAppIdAndTenantId(suiteId, authCorpId);
    }

}
