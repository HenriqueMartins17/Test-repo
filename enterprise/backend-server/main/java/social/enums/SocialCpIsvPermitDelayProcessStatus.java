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
 * WeCom service provider interface permission delay task processing status
 * </p>
 */
public enum SocialCpIsvPermitDelayProcessStatus {

    /**
     * Pending
     */
    PENDING(0),
    /**
     * Sent to queue
     */
    QUEUED(1),
    /**
     * Ordered
     */
    ORDER_CREATED(5),
    /**
     * Completed
     */
    FINISHED(9),
    ;

    private final int value;

    SocialCpIsvPermitDelayProcessStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Convert Value to Enum
     *
     * @param statusValue value
     * @return Corresponding enumeration
     */
    public static SocialCpIsvPermitDelayProcessStatus fromStatusValue(Integer statusValue) {
        switch (statusValue) {
            case 0:
                return PENDING;
            case 1:
                return QUEUED;
            case 5:
                return ORDER_CREATED;
            case 9:
                return FINISHED;
            default:
                throw new IllegalArgumentException("Unsupported status value: " + statusValue);
        }
    }

}
