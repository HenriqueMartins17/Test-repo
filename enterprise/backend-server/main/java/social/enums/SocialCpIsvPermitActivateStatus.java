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

/**
 * <p>
 * WeCom interface license account activation status
 * </p>
 */
public enum SocialCpIsvPermitActivateStatus {

    /**
     * To be activated
     */
    NO_ACTIVATED(1),
    /**
     * Active and valid
     */
    ACTIVATED(2),
    /**
     * Expired
     */
    EXPIRED(3),
    /**
     * To be transferred
     */
    TRANSFERRED(4),
    ;

    private final int value;

    SocialCpIsvPermitActivateStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Convert the activation status value from WeCom
     *
     * @param status WeCom activation status value
     * @return Corresponding enumeration
     */
    public static SocialCpIsvPermitActivateStatus fromWecomStatus(Integer status) {
        switch (status) {
            case 1:
                return NO_ACTIVATED;
            case 2:
                return ACTIVATED;
            case 3:
                return EXPIRED;
            case 4:
                return TRANSFERRED;
            default:
                throw new IllegalArgumentException("Unsupported status value: " + status);
        }
    }

}
