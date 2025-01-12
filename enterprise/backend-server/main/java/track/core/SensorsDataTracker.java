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

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Data Tracker - Sensors Implementation Class.
 * </p>
 *
 * @author Chambers
 */
public class SensorsDataTracker extends AbstractDataTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorsDataTracker.class);

    @Override
    public void track(String distinctId, String eventType,
                      Map<String, Object> properties, Cookie[] cookies) {
        // Is it a login ID, False indicates that the ID is an anonymous ID
        boolean isLoginId = true;
        if (distinctId == null) {
            isLoginId = false;
            distinctId = this.getAnonymousId(cookies);
        }
        try {
            SensorsAnalytics analytics = this.getAnalytics();
            analytics.track(distinctId, isLoginId, eventType, properties);
            analytics.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Failure to record event[{}] of user[{}].Exception message:{}",
                eventType, distinctId, e.getMessage());
        }
    }

    @Override
    public void identify(String distinctId, Cookie[] cookies) {
        this.trackSignUp(distinctId, cookies);
    }

    @Override
    public void trackSignUp(String loginId, Cookie[] cookies) {
        // Associate the anonymous ID in the logged out state with the user ID
        String anonymousId = this.getAnonymousId(cookies);
        try {
            SensorsAnalytics analytics = this.getAnalytics();
            analytics.trackSignUp(loginId, anonymousId);
            analytics.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Failure to record sensors trackSignUp event.{}",
                e.getMessage());
        }
    }

    private SensorsAnalytics getAnalytics() throws IOException {
        String hostname = InetAddress.getLocalHost().getHostName();
        File logPath = new File("/logs/sensors/" + hostname + "-logs");
        if (!logPath.exists()) {
            logPath.mkdirs();
        }
        return new SensorsAnalytics(
            new SensorsAnalytics.ConcurrentLoggingConsumer(
                logPath.getAbsolutePath() + "/service_log"));
    }

    /**
     * Get anonymous ID from cookie
     */
    private String getAnonymousId(Cookie[] cookies) {
        if (ArrayUtil.isEmpty(cookies)) {
            return Strings.EMPTY;
        }
        String value = null;
        String key = "sensorsdata2015jssdkcross";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) {
                value = cookie.getValue();
                break;
            }
        }
        if (value != null && value.length() > 0) {
            try {
                String result = new URI(value).getPath();
                JSONObject jsonObject = JSONUtil.parseObj(result);
                Object id = jsonObject.get("first_id");
                if (ObjectUtil.isNull(id)) {
                    id = jsonObject.get("distinct_id");
                }
                return id.toString();
            } catch (URISyntaxException e) {
                LOGGER.warn("Failed to parse cookie to obtain anonymous ID");
                e.printStackTrace();
            }
        }
        return Strings.EMPTY;
    }
}
