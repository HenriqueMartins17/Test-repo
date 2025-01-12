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

package com.apitable.enterprise.track.autoconfigure;

import com.apitable.enterprise.track.autoconfigure.DataAnalyzeProperties.POSTHOG;
import com.apitable.enterprise.track.core.DataTracker;
import com.apitable.enterprise.track.core.PostHogDataTracker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * PostHog AutoConfiguration Class.
 * </p>
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "data.analyze.type", havingValue = "posthog")
public class PostHogAutoConfiguration {

    private final DataAnalyzeProperties properties;

    public PostHogAutoConfiguration(DataAnalyzeProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "data.analyze.posthog", name = "host")
    public DataTracker dataTracker() {
        POSTHOG posthog = properties.getPosthog();
        return new PostHogDataTracker(posthog.getApiKey(), posthog.getHost());
    }
}
