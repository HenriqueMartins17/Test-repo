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

package com.apitable.enterprise.gm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * GM Action
 * </p>
 *
 * @author Chambers
 */
@Getter
@AllArgsConstructor
public enum GmAction {

    PERMISSION_CONFIG,

    SYSTEM_NOTIFICATION_PUBLISH,

    SYSTEM_NOTIFICATION_REVOKE,

    USER_ACTIVITY_ASSIGN,

    USER_ACTIVITY_RESET,

    TEST_CAPTCHA,

    TEST_ACCOUNT_CREATE,

    SPACE_CERTIFY,

    VALIDATION_LOCK,

    VALIDATION_UNLOCK,

    WIDGET_MANAGE,

    WIDGET_BAN,

    WIDGET_UNBAN,

    BILLING_ORDER_CREATE,

    BILLING_ORDER_QUERY,

    V_CODE_QUERY,

    V_CODE_MANAGE,

    V_CODE_COUPON_QUERY,

    V_CODE_COUPON_MANAGE,

    ACTIVITY_QUERY,

    ACTIVITY_MANAGE,

    WECHAT_QRCODE_QUERY,

    WECHAT_QRCODE__MANAGE,

    WECHAT_REPLY_RULE_REFRESH,

    INTEGRAL_REWARD,

    INTEGRAL_QUERY,

    INTEGRAL_SUBTRACT,

    CONTACT_INFO_QUERY,

    ;
}
