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

package com.apitable.enterprise.social.autoconfigure.feishu;

import cn.hutool.core.util.StrUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * feishu integration properties
 *
 * @author Shawn Deng
 */
@ConfigurationProperties(prefix = "social.feishu")
public class FeishuProperties {

    private boolean enabled = false;

    /**
     * app name
     */
    private String appName;

    /**
     * Application id.
     */
    private String appId;

    /**
     * Application secret.
     */
    private String appSecret;

    /**
     * encrypt key
     */
    private String encryptKey;

    /**
     * verification token
     */
    private String verificationToken;

    /**
     * whether is isv
     */
    private Boolean isv = Boolean.FALSE;

    /**
     * base path, default is feishu
     */
    private String basePath = "feishu";

    /**
     * callback url of event, default is event, full callback event path should be ：/{socialType}/event
     */
    private String eventPath = "event";

    /**
     * callback url of card event, default is event, full callback event path should be ：/{socialType}/card
     */
    private String cardEventPath = "card";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Boolean getIsv() {
        return isv;
    }

    public void setIsv(Boolean isv) {
        this.isv = isv;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getEventPath() {
        return eventPath;
    }

    public void setEventPath(String eventPath) {
        this.eventPath = eventPath;
    }

    public String getCardEventPath() {
        return cardEventPath;
    }

    public void setCardEventPath(String cardEventPath) {
        this.cardEventPath = cardEventPath;
    }

    /**
     * check attribute
     *
     * @param properties feishu properties
     */
    @Deprecated
    public static void checkAppProperties(FeishuProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Feishu properties should not be null, must be set");
        }

        if (StrUtil.isBlank(properties.getAppId())
                || StrUtil.isBlank(properties.getAppSecret())
                || StrUtil.isBlank(properties.getVerificationToken())) {
            throw new IllegalArgumentException("illegal App info: " + properties);
        }
    }
}
