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

package com.apitable.enterprise.vikabilling.util;

import static com.apitable.enterprise.vikabilling.enums.OrderException.PAY_ORDER_FAIL;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.vikabilling.autoconfigure.WechatFactory;
import com.apitable.enterprise.vikabilling.autoconfigure.properties.AlipayProperties;
import com.apitable.enterprise.vikabilling.autoconfigure.properties.PingProperties;
import com.apitable.enterprise.vikabilling.autoconfigure.properties.WechatpayProperties;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.util.model.ChargeDTO;
import com.pingplusplus.model.Charge;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * charge Util.
 */
@Component
public class ChargeManager {
    @Autowired(required = false)
    private PingProperties pingProperties;

    @Autowired(required = false)
    private AlipayProperties alipayProperties;

    @Autowired(required = false)
    private WechatpayProperties wechatpayProperties;

    public static String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";

    public static String WECHATPAY_TRADE_SUCCESS = "SUCCESS";

    private static final Logger log = LoggerFactory.getLogger(ChargeManager.class);

    public ChargeDTO createCharge(Price price, PayChannel channel, String outOrderNo, int amount) {
        ChargeDTO chargeDTO = new ChargeDTO();
        switch (channel) {
            case ALIPAY_PC, WX_PUB_QR -> {
                Charge charge =
                    PingppUtil.createCharge(pingProperties.getAppId(), price, channel, outOrderNo,
                        amount);
                chargeDTO.setChannelTransactionId(charge.getId());
                if (channel == PayChannel.ALIPAY_PC) {
                    chargeDTO.setAlipayPcDirectCharge(charge.toString());
                }
                if (channel == PayChannel.WX_PUB_QR) {
                    chargeDTO.setWxQrCodeLink(charge.toString());
                }
            }
            case NEW_ALIPAY_PC -> {
                try {
                    String amountStr =
                        alipayProperties.isTestMode() ? "0.01" : String.valueOf(amount / 100);
                    AlipayTradePagePayResponse response =
                        Factory.Payment.Page()
                            .optional("qr_pay_mode", 4)
                            .optional("qrcode_width", 208)
                            .pay(price.getGoodChTitle(), outOrderNo, amountStr,
                                alipayProperties.getNotifyUrl());
                    if (ResponseChecker.success(response)) {
                        // cannot get the orderNo, set it on callback
                        chargeDTO.setChannelTransactionId("");
                        chargeDTO.setAlipayPcDirectCharge(response.getBody());
                    }
                } catch (Exception e) {
                    log.error("Alipay PC error", e);
                    throw new BusinessException(PAY_ORDER_FAIL);
                }
            }
            case NEW_WX_PUB_QR -> {
                PrepayRequest request =
                    getPrepayRequest(price, outOrderNo, amount);
                PrepayResponse response = WechatFactory.Payment.nativePayService().prepay(request);
                // cannot get the orderNo, set it on callback
                chargeDTO.setChannelTransactionId("");
                chargeDTO.setWxQrCodeLink(response.getCodeUrl());
            }
        }
        return chargeDTO;

    }

    @NotNull
    private PrepayRequest getPrepayRequest(Price price, String outOrderNo, int amount) {
        PrepayRequest request = new PrepayRequest();
        Amount amountInstance = new Amount();
        amountInstance.setTotal(wechatpayProperties.isTestMode() ? 1 : amount);
        request.setAmount(amountInstance);
        request.setAppid(wechatpayProperties.getAppId());
        request.setMchid(wechatpayProperties.getMerchantId());
        request.setDescription(price.getGoodChTitle());
        request.setNotifyUrl(wechatpayProperties.getNotifyUrl());
        request.setOutTradeNo(outOrderNo);
        return request;
    }
}
