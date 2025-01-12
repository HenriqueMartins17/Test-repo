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

package com.apitable.enterprise;

import static com.apitable.enterprise.vikabilling.util.OrderUtil.yuanToCents;
import static com.apitable.enterprise.vikabilling.util.PingppUtil.createCharge;
import static com.apitable.shared.util.IdUtil.createNodeId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.apitable.AbstractIntegrationTest;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.service.IAiCreditService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.idaas.service.IIdaasAppBindService;
import com.apitable.enterprise.idaas.service.IIdaasContactService;
import com.apitable.enterprise.idaas.service.IIdaasTenantService;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderPrice;
import com.apitable.enterprise.vikabilling.entity.OrderPaymentEntity;
import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.model.OrderPaymentVo;
import com.apitable.enterprise.vikabilling.service.IBillingCapacityService;
import com.apitable.enterprise.vikabilling.service.IBillingOfflineService;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderPaymentService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.IShopService;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkOrderService;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkRefundService;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.util.ChargeManager;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker;
import com.apitable.enterprise.vikabilling.util.OrderChecker;
import com.apitable.enterprise.vikabilling.util.OrderChecker.ExpectedOrderCheck;
import com.apitable.enterprise.vikabilling.util.PingppUtil;
import com.apitable.enterprise.vikabilling.util.model.BillingPlanPrice;
import com.apitable.enterprise.vikabilling.util.model.ChargeDTO;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.config.initializers.EnterpriseEnvironmentInitializers;
import com.apitable.shared.config.properties.SystemProperties;
import com.apitable.workspace.enums.NodeType;
import com.pingplusplus.model.Charge;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.assertj.core.util.Maps;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Import(TestContextConfiguration.class)
@ContextConfiguration(initializers = {
    EnterpriseEnvironmentInitializers.class,
    TestVikaSaasContextInitializer.class
})
public abstract class AbstractVikaSaasIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected IVCodeService ivCodeService;

    @Autowired
    protected IIntegralService iIntegralService;

    @Autowired
    protected ISocialTenantBindService iSocialTenantBindService;

    @Autowired
    protected IShopService isShopService;

    @Autowired
    protected IOrderV2Service iOrderV2Service;

    @Autowired
    protected IOrderPaymentService iOrderPaymentService;

    @Autowired
    protected IGmService iGmService;

    @Autowired
    protected IBillingOfflineService iBillingOfflineService;

    @Autowired
    protected IBundleService iBundleService;

    @Autowired
    protected IBillingCapacityService iBillingCapacityService;

    @Autowired
    protected ISpaceSubscriptionService iSpaceSubscriptionService;

    @Autowired
    protected ISocialTenantService iSocialTenantService;

    @Autowired
    protected ISocialService iSocialService;

    @Autowired
    protected IAppInstanceService iAppInstanceService;

    @Autowired
    protected ISubscriptionService iSubscriptionService;

    @Autowired
    protected EntitlementChecker entitlementChecker;

    @Autowired
    protected OrderChecker orderChecker;

    @Autowired
    protected IIdaasAppBindService idaasAppBindService;

    @Autowired
    protected IIdaasContactService idaasContactService;

    @Autowired
    protected IIdaasTenantService idaasTenantService;

    @Autowired
    protected ISocialDingTalkOrderService iSocialDingTalkOrderService;

    @Autowired
    protected ISocialDingTalkRefundService iSocialDingTalkRefundService;

    @Autowired
    protected SystemProperties systemProperties;

    @Autowired
    protected IAiService iAiService;

    @Autowired
    protected IAiCreditService iAiCreditService;

    @MockBean
    protected EntitlementServiceFacade entitlementServiceFacade;

    @MockBean
    protected ChargeManager chargeManager;

    protected static final String DEFAULT_AI_NODE_NAME = "ChatBot Assistant";

    protected ZoneOffset getTestTimeZone() {
        return systemProperties.getTimeZone();
    }

    protected void autoOrderPayProcessor(Long userId, OrderArguments orderArguments,
                                         OffsetDateTime paidTime) {
        String orderId = iOrderV2Service.createOrder(orderArguments);
        // Check Order
        Price price = orderArguments.getPrice();
        Bundle actionBundle =
            iBundleService.getActivatedBundleBySpaceId(orderArguments.getSpaceId());
        OrderType orderType = iOrderV2Service.parseOrderType(actionBundle, price);
        BillingPlanPrice planPrice =
            BillingPlanPrice.of(price, ClockManager.me().getLocalDateNow());
        OrderPrice orderPrice =
            orderType == OrderType.UPGRADE ? iOrderV2Service.repairOrderPrice(actionBundle, price) :
                new OrderPrice(price.getOriginPrice(), planPrice.getDiscount(),
                    planPrice.getDiscount(), planPrice.getActual());
        ExpectedOrderCheck expected =
            new ExpectedOrderCheck(null, yuanToCents(orderPrice.getPriceOrigin()),
                yuanToCents(orderPrice.getPriceDiscount()), yuanToCents(orderPrice.getPricePaid()),
                OrderStatus.UNPAID, false, null);
        orderChecker.check(orderId, expected);

        try (
            MockedStatic<PingppUtil> pingppUtilMocked = mockStatic(PingppUtil.class)
        ) {
            Charge charge = new Charge();
            charge.setId("ch_1234567890");
            charge.setCredential(Maps.newHashMap(PayChannel.WX_PUB_QR.getName(), "mock-link"));
            pingppUtilMocked.when(
                    () -> createCharge(anyString(),
                        any(Price.class),
                        any(PayChannel.class),
                        anyString(),
                        anyInt()))
                .thenReturn(charge);
            ChargeDTO chargeDTO = new ChargeDTO();
            chargeDTO.setChannelTransactionId("");
            when(chargeManager.createCharge(any(), any(), anyString(), anyInt())).thenReturn(chargeDTO);
            // create pay order for this
            OrderPaymentVo orderPaymentVo = iOrderV2Service
                .createOrderPayment(userId, orderId, PayChannel.WX_PUB_QR);

            // trigger pay success event notify without valid pingpp signature
            OrderPaymentEntity orderPayment =
                iOrderPaymentService.getByPayTransactionId(orderPaymentVo.getPayTransactionNo());
            ChargeSuccess pingChargeSuccess = new ChargeSuccess();
            pingChargeSuccess.setId(orderPayment.getPayChannelTransactionId());
            pingChargeSuccess.setOrderNo(orderPaymentVo.getPayTransactionNo());
            pingChargeSuccess.setTimePaid(paidTime.toEpochSecond());
            iOrderPaymentService.retrieveOrderPaidEvent(pingChargeSuccess);
        }

        // check order paid
        expected = new ExpectedOrderCheck(null, yuanToCents(orderPrice.getPriceOrigin()),
            yuanToCents(orderPrice.getPriceDiscount()), yuanToCents(orderPrice.getPricePaid()),
            OrderStatus.FINISHED, true,
            paidTime.atZoneSameInstant(ZoneOffset.ofHours(8))
                .truncatedTo(ChronoUnit.SECONDS)
                .toLocalDateTime());
        orderChecker.check(orderId, expected);
    }

    protected String createAiNode(String spaceId) {
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(spaceId);
        aiObject.setAiId(aiNodeId);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        iAiService.create(aiObject);
        return aiNodeId;
    }
}
