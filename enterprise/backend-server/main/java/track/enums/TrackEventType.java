/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.track.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * buried event type
 * </p>
 *
 * @author Chambers
 */
@Getter
@AllArgsConstructor
public enum TrackEventType {

    /**
     * get the verification code successfully
     */
    GET_SMC_CODE("authGetCodeResult"),

    /**
     * registration success
     */
    REGISTER("registerSuccess"),

    /**
     * initialized nickname succeeded
     */
    SET_NICKNAME("setNameSuccess"),

    /**
     * login successful
     */
    LOGIN("loginSuccess"),

    /**
     * search template
     */
    SEARCH_TEMPLATE("searchTemplate");


    private final String eventName;
}
