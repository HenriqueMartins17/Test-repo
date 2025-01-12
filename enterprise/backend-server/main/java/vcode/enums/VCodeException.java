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

package com.apitable.enterprise.vcode.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.apitable.core.exception.BaseException;

/**
 * <p>
 * status code range（800-849）
 * </p>
 *
 * @author Chambers
 */
@Getter
@AllArgsConstructor
public enum VCodeException implements BaseException {

    ACTIVITY_NOT_EXIST(800, "activity does not exist"),

    SCENE_EXIST(800, "scene value already exists"),

    QR_CODE_EXIST(800, "The QR code has been generated, and the scene value modification failed"),

    COUPON_TEMPLATE_NOT_EXIST(801, "Coupon template does not exist"),

    EXPIRE_TIME_INCORRECT(802, "The expiration time is set incorrectly"),

    TYPE_ERROR(802, "type error"),

    TEMPLATE_EMPTY(802, "Redemption code needs to select redemption template"),

    ACCOUNT_NOT_REGISTER(802, "The specified mobile account is not registered"),

    CANNOT_ZERO(802, "The total number of codes that can be used, and the number of times that a single person can use cannot be zero"),

    CODE_NOT_EXIST(803, "code does not exist"),

    TYPE_INFO_ERROR(803, "Non-redemption code type, failed to modify the redemption template"),

    INVITE_CODE_NOT_EXIST(804, "Please enter the invitation code"),

    INVITE_CODE_NOT_VALID(805, "Invitation code is invalid"),

    INVITE_CODE_EXPIRE(805, "Invitation code has expired"),

    INVITE_CODE_USED(805, "Invitation code used"),

    REDEMPTION_CODE_NOT_EXIST(804, "Please enter redemption code"),

    REDEMPTION_CODE_NOT_VALID(805, "Invalid redemption code"),

    REDEMPTION_CODE_EXPIRE(805, "Redemption code has expired"),

    REDEMPTION_CODE_USED(805, "Redemption code used"),

    INVITE_CODE_REWARD_ERROR(805, "Invitation code registration reward failed"),

    INVITE_CODE_FREQUENTLY(805, "Invitation code is frequently used for registration");

    private final Integer code;

    private final String message;
}
