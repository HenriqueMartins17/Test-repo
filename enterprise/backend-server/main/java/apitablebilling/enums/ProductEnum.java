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

package com.apitable.enterprise.apitablebilling.enums;

import static com.apitable.enterprise.apitablebilling.enums.ProductCategory.BASE;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;


/**
 * product define.
 *
 * @author Shawn Deng
 */
@Getter
public enum ProductEnum {

    FREE("Free", BASE, true),

    STARTER("Starter", BASE),

    PLUS("Plus", BASE),

    PRO("Pro", BASE),

    BUSINESS("Business", BASE),

    ENTERPRISE("Enterprise", BASE),

    COMMUNITY("Community", BASE, true),

    APITABLE_ENTERPRISE("APITable Enterprise", BASE),

    AITABLE_PREMIUM("AITable Premium", BASE),

    PRIVATE_CLOUD("Private_Cloud", BASE),

    APPSUMO_TIER1("appsumo_tier1", BASE),

    APPSUMO_TIER2("appsumo_tier2", BASE),

    APPSUMO_TIER3("appsumo_tier3", BASE),

    APPSUMO_TIER4("appsumo_tier4", BASE),

    APPSUMO_TIER5("appsumo_tier5", BASE),

    EXCLUSIVE_LIMITED_TIER1("Exclusive Limited Tier 1", BASE),

    EXCLUSIVE_LIMITED_TIER2("Exclusive Limited Tier 2", BASE),

    EXCLUSIVE_LIMITED_TIER3("Exclusive Limited Tier 3", BASE),

    EXCLUSIVE_LIMITED_TIER4("Exclusive Limited Tier 4", BASE),

    EXCLUSIVE_LIMITED_TIER5("Exclusive Limited Tier 5", BASE),
    ;

    private final String name;

    private final ProductCategory category;

    private final boolean free;

    ProductEnum(String name, ProductCategory category) {
        this(name, category, false);
    }

    ProductEnum(String name, ProductCategory category, boolean free) {
        this.name = name;
        this.category = category;
        this.free = free;
    }

    /**
     * transform from value.
     *
     * @param name enum name
     * @return ProductEnum
     */
    @JsonCreator
    public static ProductEnum of(String name) {
        for (ProductEnum value : ProductEnum.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unsupported product name");
    }

    /**
     * check is appsumo product.
     *
     * @param name product name
     * @return boolean
     */
    public static boolean isAppsumoProduct(String name) {
        return APPSUMO_TIER1.name.equals(name) || APPSUMO_TIER2.name.equals(name)
            || APPSUMO_TIER3.name.equals(name) || APPSUMO_TIER4.name.equals(name)
            || APPSUMO_TIER5.name.equals(name);
    }

    /**
     * check is exclusive limit product.
     *
     * @param name product name
     * @return boolean
     */
    public static boolean isExclusiveLimitProduct(String name) {
        return EXCLUSIVE_LIMITED_TIER1.name.equals(name) || EXCLUSIVE_LIMITED_TIER2.name.equals(name)
            || EXCLUSIVE_LIMITED_TIER3.name.equals(name) || EXCLUSIVE_LIMITED_TIER4.name.equals(name)
            || EXCLUSIVE_LIMITED_TIER5.name.equals(name);
    }
}
