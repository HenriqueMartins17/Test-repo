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

import java.util.Objects;

/**
 * <p>
 * Third party platform authorization mode
 * </p>
 */
public enum SocialTenantAuthMode {

    /**
     * Enterprise administrator authorization
     */
    ADMIN(1),
    /**
     * Member Authorization
     */
    MEMBER(2),
    ;

    private final int value;

    SocialTenantAuthMode(int value) {
        this.value = value;
    }

    /**
     * Convert the authorization mode value defined by WeCom to the current enumeration
     *
     * @param authMode Value of authorization mode defined by WeCom
     * @return {@link SocialTenantAuthMode}
     * @throws IllegalArgumentException The parameter is {@code null} or invalid value
     */
    public static SocialTenantAuthMode fromWeCom(Integer authMode) {

        Objects.requireNonNull(authMode, "Auth mode from WeCom cannot be null.");

        if (authMode == 0) {
            return ADMIN;
        }
        else if (authMode == 1) {
            return MEMBER;
        }

        throw new IllegalArgumentException("Unsupported auth mode from WeCom: " + authMode);

    }

    public static SocialTenantAuthMode fromTenant(Integer authMode) {

        Objects.requireNonNull(authMode, "Auth mode from tenant cannot be null.");

        if (authMode == 1) {
            return ADMIN;
        }
        else if (authMode == 2) {
            return MEMBER;
        }

        throw new IllegalArgumentException("Unsupported auth mode from tenant: " + authMode);

    }

    public int getValue() {
        return value;
    }

}
