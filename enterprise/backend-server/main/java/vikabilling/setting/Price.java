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

package com.apitable.enterprise.vikabilling.setting;

import java.math.BigDecimal;

import lombok.Data;

/**
 * <p>
 * Billing Price
 * </p>
 */
@Data
public class Price {

    private String id;

    private String goodEnTitle;

    private String goodChTitle;

    private String planId;

    private Integer month;

    private String product;

    private Integer seat;

    private String seatDesc;

    private boolean online;

    private String priceListId;

    private BigDecimal originPrice;
}
