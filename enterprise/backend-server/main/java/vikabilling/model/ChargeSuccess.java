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

package com.apitable.enterprise.vikabilling.model;

import static com.apitable.enterprise.vikabilling.enums.PayChannel.NEW_ALIPAY_PC;
import static com.apitable.enterprise.vikabilling.enums.PayChannel.NEW_WX_PUB_QR;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.vikabilling.util.model.AlipayTradeNotifyDTO;
import com.apitable.shared.clock.spring.ClockManager;
import com.pingplusplus.model.Charge;
import com.wechat.pay.java.service.payments.model.Transaction;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.Data;

/**
 * payment success notification.
 */
@Data
public class ChargeSuccess {

    private String id;

    private String channel;

    private String orderNo;

    private int amount;

    private int amountSettle;

    private String currency;

    private String subject;

    private String body;

    private Long timePaid;

    private Long timeExpire;

    private String transactionNo;

    /**
     * PingChargeSuccess.
     */
    public static ChargeSuccess build(Charge charge) {
        ChargeSuccess chargeSuccess = new ChargeSuccess();
        chargeSuccess.setId(charge.getId());
        chargeSuccess.setChannel(charge.getChannel());
        chargeSuccess.setOrderNo(charge.getOrderNo());
        chargeSuccess.setAmount(charge.getAmount());
        chargeSuccess.setAmountSettle(charge.getAmountSettle());
        chargeSuccess.setCurrency(charge.getCurrency());
        chargeSuccess.setSubject(charge.getSubject());
        chargeSuccess.setBody(charge.getBody());
        chargeSuccess.setTimePaid(charge.getTimePaid());
        chargeSuccess.setTimeExpire(charge.getTimeExpire());
        chargeSuccess.setTransactionNo(charge.getTransactionNo());
        return chargeSuccess;
    }

    public static ChargeSuccess buildWithAlipay(AlipayTradeNotifyDTO tradeNotifyDTO) {
        ChargeSuccess chargeSuccess = new ChargeSuccess();
        chargeSuccess.setId(tradeNotifyDTO.getOutTradeNo());
        chargeSuccess.setChannel(NEW_ALIPAY_PC.getName());
        chargeSuccess.setOrderNo(tradeNotifyDTO.getOutTradeNo());
        chargeSuccess.setAmount((int) (tradeNotifyDTO.getTotalAmount() * 100));
        chargeSuccess.setAmountSettle((int) (tradeNotifyDTO.getInvoiceAmount() * 100));
        chargeSuccess.setSubject(tradeNotifyDTO.getSubject());
        chargeSuccess.setBody(tradeNotifyDTO.getBody());
        chargeSuccess.setTimePaid(
            ClockManager.me().convertUnixTimeToMillis(tradeNotifyDTO.getGmtPayment()));
        chargeSuccess.setTimePaid(
            tradeNotifyDTO.getGmtPayment().atZone(ZoneOffset.ofHours(8)).toEpochSecond());
        chargeSuccess.setTransactionNo(tradeNotifyDTO.getTradeNo());
        return chargeSuccess;
    }

    public static ChargeSuccess buildWithWechatpay(Transaction transaction) {
        ChargeSuccess chargeSuccess = new ChargeSuccess();
        chargeSuccess.setId(transaction.getOutTradeNo());
        chargeSuccess.setChannel(NEW_WX_PUB_QR.getName());
        chargeSuccess.setOrderNo(transaction.getOutTradeNo());
        chargeSuccess.setAmount(transaction.getAmount().getPayerTotal());
        chargeSuccess.setAmountSettle(transaction.getAmount().getPayerTotal());
        chargeSuccess.setTimePaid(
            LocalDateTime.parse(transaction.getSuccessTime(), ISO_OFFSET_DATE_TIME)
                .atZone(ZoneOffset.ofHours(8)).toEpochSecond());
        chargeSuccess.setTransactionNo(transaction.getTransactionId());
        return chargeSuccess;
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
