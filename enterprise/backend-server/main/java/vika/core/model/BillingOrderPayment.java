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

package com.apitable.enterprise.vika.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.apitable.enterprise.vika.core.jackson.LocalDateTimeToUnixSerializer;
import com.apitable.enterprise.vika.core.jackson.PayChannelSerializer;

public class BillingOrderPayment {

    @JsonProperty("订单ID")
    private String orderId;

    @JsonProperty("交易ID")
    private String paymentTransactionId;

    @JsonProperty("金额")
    private BigDecimal amount;

    @JsonProperty("支付渠道")
    @JsonSerialize(using = PayChannelSerializer.class)
    private String payChannel;

    @JsonProperty("PPP交易ID")
    private String payChannelTransactionId;

    @JsonProperty("支付时间")
    @JsonSerialize(using = LocalDateTimeToUnixSerializer.class)
    private LocalDateTime paidTime;

    @JsonProperty("元数据")
    private String rawData;

    @JsonProperty("所属订单")
    private List<String> orderIds;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getPayChannelTransactionId() {
        return payChannelTransactionId;
    }

    public void setPayChannelTransactionId(String payChannelTransactionId) {
        this.payChannelTransactionId = payChannelTransactionId;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }
}
