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

package com.apitable.enterprise.track.core;


import java.util.Map;
import jakarta.servlet.http.Cookie;

/**
 * <p>
 * Data Tracker
 * </p>
 *
 * @author Chambers
 */
public interface DataTracker {

    /**
     * Track events.
     *
     * @param distinctId distinct id
     * @param eventName  event name
     * @param properties event properties
     */
    void track(String distinctId, String eventName,
               Map<String, Object> properties, Cookie[] cookies);

    /**
     * Identify user.
     *
     * @param distinctId distinct id
     * @param cookies    cookies
     * @author Chambers
     */
    void identify(String distinctId, Cookie[] cookies);

    /**
     * Log user registration events.
     *
     * @param loginId login ID
     * @param cookies cookies
     */
    void trackSignUp(String loginId, Cookie[] cookies);
}
