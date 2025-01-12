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

package com.apitable.enterprise.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.apitable.core.exception.BaseException;

/**
 * <p>
 *  status code range（700-799）
 * </p>
 *
 * @author Benson Cheung
 */
@Getter
@AllArgsConstructor
public enum WechatException implements BaseException {

	ILLEGAL_REQUEST(700, "Illegal request, possibly a forged request"),

    UPDATE_AUTO_REPLY_ERROR(701, "Official account automatic reply rules and keyword update failed");

    private final Integer code;

    private final String message;
}
