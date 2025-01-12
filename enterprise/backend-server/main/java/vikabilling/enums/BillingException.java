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
 * <p>
 * billing exception
 * </p>
 *
 * @author Shawn Deng
 */
@Getter
@AllArgsConstructor
public enum BillingException implements BaseException {

    ACCOUNT_BUNDLE_ERROR(901, "account subscription exception"),

    PLAN_FEATURE_NOT_SUPPORT(901, "subscription feature not supported"),

    PLAN_FEATURE_OVER_LIMIT(901, "subscription feature exceeds limit"),

    ACCOUNT_CREDIT_ALTER_FREQUENTLY(901, "Account points are operated too frequently, please try again later"),

    SELF_HOST_SUBSCRIPTION_EXPIRED(902, "The subscription has expired, please contact the administrator to renew the subscription");

    private final Integer code;

    private final String message;
}
