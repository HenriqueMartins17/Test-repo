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

package com.apitable.enterprise.social.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.apitable.core.exception.BusinessException;

/**
 * Social platform type
 *
 * @author Shawn Deng
 */
@Getter
@AllArgsConstructor
public enum SocialPlatformType {

    WECOM(1),

    DINGTALK(2),

    FEISHU(3),

    WOA(10);

    private final Integer value;

    public static SocialPlatformType toEnum(Integer type) {
        for (SocialPlatformType e : SocialPlatformType.values()) {
            if (e.getValue().equals(type)) {
                return e;
            }
        }
        throw new BusinessException("unknown social platform type");
    }
}
