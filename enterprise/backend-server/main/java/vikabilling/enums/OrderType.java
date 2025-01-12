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

public enum OrderType {

    BUY("buy", 0),
    UPGRADE("upgrade", 1),
    RENEW("renew", 2);

    private final String name;

    private final int type;

    OrderType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public static OrderType ofName(String name) {
        for (OrderType value : OrderType.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static OrderType of(String name) {
        for (OrderType value : OrderType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static OrderType ofType(Integer type) {
        for (OrderType value : OrderType.values()) {
            if (value.getType() == type) {
                return value;
            }
        }
        return null;
    }
}
