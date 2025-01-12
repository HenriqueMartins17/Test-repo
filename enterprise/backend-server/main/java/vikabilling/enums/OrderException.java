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

package com.apitable.enterprise.vikabilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.apitable.core.exception.BaseException;

/**
 * Order exception
 * 1400 - 1499
 * @author Shawn Deng
 */
@Getter
@AllArgsConstructor
public enum OrderException implements BaseException {

    PLAN_NOT_EXIST(1401, "paid plan does not exist"),

    ORDER_NOT_EXIST(1402, "order does not exist"),

    ORDER_HAS_CANCELED(1403, "order cancelled"),

    ORDER_HAS_PAID(1404, "order paid"),

    PAY_ORDER_FAIL(1405, "payment order failed"),

    PAY_CHANNEL_ERROR(1406, "wrong payment method"),

    ORDER_EXCEPTION(1408, "order exception"),

    PAYMENT_ORDER_NOT_EXIST(1409, "payment order does not exist"),

    NOT_ALLOW_DOWNGRADE(1410, "subscription downgrades are not allowed"),

    REPEAT_NEW_BUY_ORDER(1411, "duplicate new purchase order");

    private final Integer code;

    private final String message;
}
