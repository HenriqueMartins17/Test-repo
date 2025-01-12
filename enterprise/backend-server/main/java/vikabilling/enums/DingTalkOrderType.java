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

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DingTalkOrderType {

    BUY("新购"),

    RENEW("续费"),

    UPGRADE("升级"),

    RENEW_UPGRADE("续费升配"),

    RENEW_DEGRADE("续费降配"),

    UPGRADE_CLOSED("升级关闭"),

    DUE_CLOSE("到期关闭"),

    REFUND_CLOSE("退款关闭"),

    OTHER_CLOSE("其他关闭");

    private final String name;

    public String getValue() {
        return this.name;
    }

    public static DingTalkOrderType getType(String name) {
        for (DingTalkOrderType type : DingTalkOrderType.values()) {
            if (type.getValue().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
