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

/**
 * billing function type
 * @author Shawn Deng
 */
@Getter
@RequiredArgsConstructor
public enum BillingFunctionType {

    SUBSCRIBE("subscribe"),

    CONSUME("consume"),

    SOLID("solid"),

    USAGE("usage");

    private final String type;

    public static BillingFunctionType of(String type) {
        for (BillingFunctionType e : BillingFunctionType.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

    public boolean isConsume() {
        return "consume".equals(type);
    }

    public boolean isSubscribe() {
        return "subscribe".equals(type);
    }

    public boolean isSolid() {
        return "solid".equals(type);
    }

    public boolean isUsage() {
        return "usage".equals(type);
    }
}
