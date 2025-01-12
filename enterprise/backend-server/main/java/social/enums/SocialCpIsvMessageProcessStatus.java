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
 * Third party platform integration - WeCom third-party service provider application message notification processing status
 * </p>
 */
public enum SocialCpIsvMessageProcessStatus {

    /**
     * Pending
     */
    PENDING(1),
    /**
     * Processing failed, need to retry
     */
    REJECT_TEMPORARILY(2),
    /**
     * Processing failed, end
     */
    REJECT_PERMANENTLY(3),
    /**
     * Processing succeeded
     */
    SUCCESS(4),
    ;

    private final int value;

    SocialCpIsvMessageProcessStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
