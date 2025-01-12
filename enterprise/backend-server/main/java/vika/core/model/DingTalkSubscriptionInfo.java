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

/**
 * <p>
 * DingTalk subscription information
 * </p>
 *
 */
public class DingTalkSubscriptionInfo {

    private String spaceId;

    private String spaceName;

    private String orderType;

    private String goodsCode;

    private String subscriptionType;

    private Integer seat;

    private Long serviceStartTime;

    private Long serviceStopTime;

    private String data;

    /**
     * order label
     * 0：common
     * 1：full gift
     */
    private Integer orderLabel;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Integer getSeat() {
        return seat;
    }

    public void setSeat(Integer seat) {
        this.seat = seat;
    }

    public Long getServiceStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(Long serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public Long getServiceStopTime() {
        return serviceStopTime;
    }

    public void setServiceStopTime(Long serviceStopTime) {
        this.serviceStopTime = serviceStopTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getOrderLabel() {
        return orderLabel;
    }

    public void setOrderLabel(Integer orderLabel) {
        this.orderLabel = orderLabel;
    }
}
