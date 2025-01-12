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

package com.apitable.enterprise.appstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.apitable.core.exception.BaseException;

@Getter
@AllArgsConstructor
public enum AppException implements BaseException {

    APP_NOT_EXIST(1301, "app does not exist"),

    APP_EXIST(1302, "app already exists"),

    APP_INSTANCE_NOT_EXIST(1303, "application instance does not exist"),

    NOT_LARK_APP_TYPE(1304, "not lark app type"),

    APP_NOT_OPEN(1305, "app is not open"),

    APP_KEY_EXIST(1306, "application instance configuration already exists");

    private final Integer code;

    private final String message;
}
