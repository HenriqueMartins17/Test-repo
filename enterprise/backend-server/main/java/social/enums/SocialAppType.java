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

/**
 * <p>
 * Application type of social media platform
 * </p>
 */
@Getter
@AllArgsConstructor
public enum SocialAppType {

    /**
     * Enterprise internal application
     */
    INTERNAL(1),

    /**
     * Independent service provider
     */
    ISV(2);

    private final int type;

    public static SocialAppType of(int value) {
        for (SocialAppType socialAppType : SocialAppType.values()) {
            if (socialAppType.type == value) {
                return socialAppType;
            }
        }
        return null;
    }
}
