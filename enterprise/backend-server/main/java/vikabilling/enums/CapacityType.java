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

/**
 * <p>
 * capacity type
 * </p>
 *
 * @author liuzijing
 */
public enum CapacityType {

    SUBSCRIPTION_PACKAGE_CAPACITY("subscription_package_capacity", 0),

    OFFICIAL_GIFT_CAPACITY("official_gift_capacity", 1),

    PARTICIPATION_CAPACITY("participation_capacity", 2),

    PURCHASE_CAPACITY("purchase_capacity", 3);

    private final String name;

    private final int type;

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    CapacityType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public static CapacityType of(String name) {
        for (CapacityType value : CapacityType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
