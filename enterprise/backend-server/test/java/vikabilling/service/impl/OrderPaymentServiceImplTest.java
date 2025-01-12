package com.apitable.enterprise.vikabilling.service.impl;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.vikabilling.core.DefaultOrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.model.CreateOrderRo;
import com.apitable.enterprise.vikabilling.model.OrderPaymentVo;
import com.apitable.enterprise.vikabilling.util.model.ChargeDTO;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.wechat.pay.java.service.payments.model.Transaction;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class OrderPaymentServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    void testWechatpayCallback() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        CreateOrderRo ro = new CreateOrderRo();
        ro.setSeat(2);
        ro.setMonth(1);
        ro.setProduct(ProductEnum.SILVER.name());
        ro.setSpaceId(userSpace.getSpaceId());
        final OrderArguments orderArguments = new DefaultOrderArguments(ro);
        String orderId = iOrderV2Service.createOrder(orderArguments);
        ChargeDTO chargeDTO = new ChargeDTO();
        chargeDTO.setChannelTransactionId("");
        chargeDTO.setWxQrCodeLink("wxlink://");
        when(chargeManager.createCharge(any(), any(), anyString(), anyInt())).thenReturn(chargeDTO);
        OrderPaymentVo paymentVo =
            iOrderV2Service.createOrderPayment(userSpace.getUserId(), orderId,
                PayChannel.NEW_WX_PUB_QR);
        Transaction transaction =
            getWechatpayData("enterprise/vikabilling/wechat_callback_data.json");
        assert transaction != null;
        transaction.setOutTradeNo(paymentVo.getPayTransactionNo());
        iOrderPaymentService.retrieveOrderPaidEvent(
            ChargeSuccess.buildWithWechatpay(transaction));
        SubscriptionInfo subscriptionInfo =
            iSpaceSubscriptionService.getPlanInfoBySpaceId(userSpace.getSpaceId());
        assertThat(subscriptionInfo.getProduct()).isEqualTo(ProductEnum.SILVER.getName());


    }

    private Transaction getWechatpayData(String filePath) {
        InputStream resourceAsStream =
            ClassPathResource.class.getClassLoader().getResourceAsStream(filePath);
        if (resourceAsStream == null) {
            return null;
        }
        String data = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        return BeanUtil.toBean(JSONUtil.parse(data), Transaction.class);
    }
}
