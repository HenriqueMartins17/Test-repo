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

public class BillingOrderItem {

    @JsonProperty("订单ID")
    private String orderId;

    @JsonProperty("产品")
    private String productName;

    @JsonProperty("产品类型")
    private String productCategory;

    @JsonProperty("席位")
    private Integer seat;

    @JsonProperty("订阅月数")
    private Integer months;

    @JsonProperty("开始时间")
    @JsonSerialize(using = LocalDateTimeToUnixSerializer.class)
    private LocalDateTime startDate;

    @JsonProperty("结束时间")
    @JsonSerialize(using = LocalDateTimeToUnixSerializer.class)
    private LocalDateTime endDate;

    @JsonProperty("金额")
    private BigDecimal amount;

    @JsonProperty("所属订单")
    private List<String> orderIds;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public Integer getSeat() {
        return seat;
    }

    public void setSeat(Integer seat) {
        this.seat = seat;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }
}
