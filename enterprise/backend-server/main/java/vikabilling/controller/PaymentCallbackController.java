/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.controller;

import static com.apitable.enterprise.vikabilling.constants.BillingConstants.WECHATPAY_HEADER_NONCE;
import static com.apitable.enterprise.vikabilling.constants.BillingConstants.WECHATPAY_HEADER_SERIAL;
import static com.apitable.enterprise.vikabilling.constants.BillingConstants.WECHATPAY_HEADER_SIGNATURE;
import static com.apitable.enterprise.vikabilling.constants.BillingConstants.WECHATPAY_HEADER_TIMESTAMP;
import static com.apitable.enterprise.vikabilling.util.ChargeManager.ALIPAY_TRADE_SUCCESS;
import static com.apitable.enterprise.vikabilling.util.ChargeManager.WECHATPAY_TRADE_SUCCESS;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.vikabilling.autoconfigure.WechatFactory;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.service.IOrderPaymentService;
import com.apitable.enterprise.vikabilling.util.PingppUtil;
import com.apitable.enterprise.vikabilling.util.model.AlipayTradeNotifyDTO;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.pingplusplus.model.Event;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment Callback Module API.
 */
@RestController
@Tag(name = "Payment Callback Module API")
@ApiResource
@Slf4j
public class PaymentCallbackController {

    @Resource
    private IOrderPaymentService iOrderPaymentService;

    @PostResource(path = "/order/paid/callback", requiredLogin = false)
    @Operation(summary = "Payment Success WebHook Notification", description = "Ping++", hidden = true)
    public String orderPaid(@RequestHeader HttpHeaders headers, HttpServletRequest request)
        throws Exception {
        return paySuccessCallback(headers, request);
    }

    @PostResource(path = "/order/alipay/callback", requiredLogin = false)
    @Operation(summary = "Alipay Payment Success WebHook Notification", description = "Alipay", hidden = true)
    public String alipayCallback(@RequestParam Map<String, String> parameters)
        throws Exception {
        Boolean result = Factory.Payment.Common().verifyNotify(parameters);
        if (!result) {
            return "fail";
        }
        AlipayTradeNotifyDTO event =
            BeanUtil.fillBeanWithMap(parameters, new AlipayTradeNotifyDTO(), true, true);
        if (ALIPAY_TRADE_SUCCESS.equals(event.getTradeStatus())) {
            ChargeSuccess chargeSuccess = ChargeSuccess.buildWithAlipay(event);
            // payment successful
            String orderId = iOrderPaymentService.retrieveOrderPaidEvent(chargeSuccess);
            // Sync order events
            if (StrUtil.isNotBlank(orderId)) {
                SpringContextHolder.getApplicationContext()
                    .publishEvent(new SyncOrderEvent(this, orderId));
                return "success";
            }
        }
        return "fail";
    }


    @PostResource(path = "/order/wechatpay/callback", requiredLogin = false)
    @Operation(summary = "Wechatpay Success WebHook Notification", description = "Wechatpay", hidden = true)
    public void wechatpayCallback(@RequestHeader HttpHeaders headers, HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        String requestBody = HttpContextUtil.getBody(request);

        com.wechat.pay.java.core.notification.RequestParam requestParam =
            new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(headers.getFirst(WECHATPAY_HEADER_SERIAL))
                .nonce(headers.getFirst(WECHATPAY_HEADER_NONCE))
                .signature(headers.getFirst(WECHATPAY_HEADER_SIGNATURE))
                .timestamp(headers.getFirst(WECHATPAY_HEADER_TIMESTAMP))
                .body(requestBody)
                .build();
        try {
            Transaction transaction =
                WechatFactory.Payment.notificationParser().parse(requestParam, Transaction.class);
            if (WECHATPAY_TRADE_SUCCESS.equals(transaction.getTradeState().name())) {
                ChargeSuccess chargeSuccess = ChargeSuccess.buildWithWechatpay(transaction);
                // payment successful
                String orderId = iOrderPaymentService.retrieveOrderPaidEvent(chargeSuccess);
                // Sync order events
                if (StrUtil.isNotBlank(orderId)) {
                    SpringContextHolder.getApplicationContext()
                        .publishEvent(new SyncOrderEvent(this, orderId));
                    response.setStatus(HttpStatus.OK.value());
                }
            }
        } catch (ValidationException e) {
            log.error("wechatpay sign verification failed", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        response.setStatus(HttpStatus.OK.value());
        ;
    }

    private String paySuccessCallback(HttpHeaders headers, HttpServletRequest request)
        throws Exception {
        String signature = headers.getFirst(PingppUtil.PINGPP_SIGNATURE);
        if (log.isDebugEnabled()) {
            log.debug("signature value：{}", signature);
        }
        String requestBody = HttpContextUtil.getBody(request);
        if (StrUtil.isBlank(signature) && JSONUtil.parseObj(requestBody).isEmpty()) {
            // Verify the address request and return directly
            return "pingxx:success";
        }
        // Parse asynchronous notification data
        Event event = PingppUtil.getEventFromRequest(requestBody, signature);
        if (log.isDebugEnabled()) {
            log.debug("Event body:{}", event.toString());
        }
        if (PingppUtil.CHARGE_SUCCESS.equals(event.getType())) {
            // payment successful
            ChargeSuccess chargeSuccess =
                PingppUtil.parsePingChargeSuccessData(event.getData().getObject().toString());
            String orderId = iOrderPaymentService.retrieveOrderPaidEvent(chargeSuccess);
            // Sync order events
            if (StrUtil.isNotBlank(orderId)) {
                SpringContextHolder.getApplicationContext()
                    .publishEvent(new SyncOrderEvent(this, orderId));
            }
        }
        return "pingxx:success";
    }
}
