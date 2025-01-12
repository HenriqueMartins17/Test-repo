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
 * Delay processing type
 * </p>
 */
public enum SocialCpIsvPermitDelayType {

    /**
     * Interface license free trial expiration notice
     */
    NOTIFY_BEFORE_TRIAL_EXPIRED(1),
    /**
     * Enterprise pays for delayed purchase of interface license
     */
    BUY_AFTER_SUBSCRIPTION_PAID(2),
    ;

    private final int value;

    SocialCpIsvPermitDelayType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
