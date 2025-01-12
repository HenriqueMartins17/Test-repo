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

package com.apitable.enterprise.vikabilling.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayChannel {

    ALIPAY_PC("alipay_pc_direct"),

    WX_PUB_QR("wx_pub_qr"),

    NEW_ALIPAY_PC("new_alipay_pc_direct"),

    NEW_WX_PUB_QR("new_wx_pub_qr");

    private final String name;

    public static PayChannel of(String name) {
        for (PayChannel value : PayChannel.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isWechatpay(PayChannel name) {
        return WX_PUB_QR.equals(name) || NEW_WX_PUB_QR.equals(name);
    }

    public static boolean isAlipay(PayChannel name) {
        return ALIPAY_PC.equals(name) || NEW_ALIPAY_PC.equals(name);
    }
}
