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

package com.apitable.enterprise.social.autoconfigure.qq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tencent.webapp")
public class WebAppProperties {

    /**
     * whether to enable, default false
     */
    private boolean enabled = false;

    /**
     * appId
     */
    private String appId;

    /**
     * appKey
     */
    private String appKey;

    /**
     * redirect uri
     */
    private String redirectUri;

    /**
     * Whether union ID is applied
     */
    private boolean applyUnion = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public boolean isApplyUnion() {
        return applyUnion;
    }

    public void setApplyUnion(boolean applyUnion) {
        this.applyUnion = applyUnion;
    }
}
