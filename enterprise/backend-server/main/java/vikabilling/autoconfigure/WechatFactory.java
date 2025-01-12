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

package com.apitable.enterprise.vikabilling.autoconfigure;

import com.apitable.enterprise.vikabilling.autoconfigure.properties.WechatpayProperties;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;

public class WechatFactory {

    private static Config config;

    public static void setConfig(WechatpayProperties wechatpayProperties) {
        config = new RSAAutoCertificateConfig.Builder()
            .privateKey(wechatpayProperties.getPrivateKey())
            .merchantId(wechatpayProperties.getMerchantId())
            .merchantSerialNumber(wechatpayProperties.getMerchantSerialNumber())
            .apiV3Key(wechatpayProperties.getApiV3Key())
            .build();

    }

    public static class Payment {

        /**
         * jsapi pay service.
         *
         * @return JsapiService
         */
        public static JsapiService jsapiService() {
            return new JsapiService.Builder().config(config).build();
        }

        /**
         * native pay service, qr code
         *
         * @return JsapiService
         */
        public static NativePayService nativePayService() {
            return new NativePayService.Builder().config(config).build();
        }

        /**
         * notification parser.
         */
        public static NotificationParser notificationParser() {
            return new NotificationParser((NotificationConfig) config);
        }
    }
}
