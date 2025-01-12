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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Data analysis configuration properties.
 * </p>
 *
 * @author Chambers
 */
@ConfigurationProperties(prefix = "data.analyze")
public class DataAnalyzeProperties {

    private boolean enabled = false;

    private DataAnalyzeType type;

    private POSTHOG posthog;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DataAnalyzeType getType() {
        return type;
    }

    public void setType(DataAnalyzeType type) {
        this.type = type;
    }

    public POSTHOG getPosthog() {
        return posthog;
    }

    public void setPosthog(POSTHOG posthog) {
        this.posthog = posthog;
    }

    public enum DataAnalyzeType {

        SENSORS,

        POSTHOG
    }

    public static class POSTHOG {
        private String apiKey;

        private String host;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }
}
