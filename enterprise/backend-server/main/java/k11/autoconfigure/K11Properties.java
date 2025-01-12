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

package com.apitable.enterprise.k11.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * connector k11 properties
 * </p>
 *
 * @author Chambers
 */
@ConfigurationProperties(prefix = "connector.k11")
public class K11Properties {

    private boolean enabled = false;

    private String domain;

    private String appId;

    private String appSecret;

    private String smsTempCode;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getSmsTempCode() {
        return smsTempCode;
    }

    public void setSmsTempCode(String smsTempCode) {
        this.smsTempCode = smsTempCode;
    }
}
