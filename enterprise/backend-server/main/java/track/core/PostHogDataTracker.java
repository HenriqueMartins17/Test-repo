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

import com.posthog.java.PostHog;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Data Tracker - PostHog Implementation Class.
 * </p>
 *
 * @author Chambers
 */
public class PostHogDataTracker extends AbstractDataTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostHogDataTracker.class);

    private final String apiKey;

    private final String host;

    public PostHogDataTracker(String apiKey, String host) {
        this.apiKey = apiKey;
        this.host = host;
    }

    @Override
    public void track(String distinctId, String eventType,
                      Map<String, Object> properties, Cookie[] cookies) {
        if (distinctId == null) {
            return;
        }
        try {
            PostHog posthog = new PostHog.Builder(apiKey).host(host).build();
            // run commands
            posthog.capture(distinctId, eventType, properties);
            // send the last events in queue
            posthog.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Failure to record event[{}] of user[{}].Exception message:{}",
                eventType, distinctId, e.getMessage());
        }
    }

    @Override
    public void identify(String distinctId, Cookie[] cookies) {
        try {
            PostHog posthog = new PostHog.Builder(apiKey).host(host).build();
            // run commands
            posthog.identify(distinctId, new HashMap<>());
            // send the last events in queue
            posthog.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Failure to record identify event of user[{}].Exception message:{}",
                distinctId, e.getMessage());
        }
    }
}
