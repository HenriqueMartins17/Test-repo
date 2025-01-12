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

package com.apitable.enterprise.vikabilling.autoconfigure.properties;

import com.alipay.easysdk.kernel.Config;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * alipay properties
 */
@Getter
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayProperties {

    private boolean isTestMode = false;

    private boolean enabled = false;

    /**
     * application private key
     */
    private String merchantPrivateKey;

    /**
     * alipay public key path
     */
    private String alipayPublicKey;

    private String appId;

    private String notifyUrl;

    public void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMerchantPrivateKey(String merchantPrivateKey) {
        this.merchantPrivateKey = merchantPrivateKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Config getOptions() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipay.com";
        config.signType = "RSA2";
        config.appId = appId;
        config.alipayPublicKey = alipayPublicKey;
        config.merchantPrivateKey = merchantPrivateKey;
        config.notifyUrl = notifyUrl;
        return config;
    }
}
