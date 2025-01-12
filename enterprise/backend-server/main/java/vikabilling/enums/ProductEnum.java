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

import static com.apitable.enterprise.vikabilling.enums.ProductCategory.ADD_ON;
import static com.apitable.enterprise.vikabilling.enums.ProductCategory.BASE;
import static com.apitable.enterprise.vikabilling.enums.ProductChannel.ALIYUN;
import static com.apitable.enterprise.vikabilling.enums.ProductChannel.DINGTALK;
import static com.apitable.enterprise.vikabilling.enums.ProductChannel.LARK;
import static com.apitable.enterprise.vikabilling.enums.ProductChannel.VIKA;
import static com.apitable.enterprise.vikabilling.enums.ProductChannel.WECOM;

import lombok.Getter;


/**
 * product define enum.
 *
 * @author Shawn Deng
 */
@Getter
public enum ProductEnum {

    BRONZE("Bronze", BASE, VIKA, 1, true),
    SILVER("Silver", BASE, VIKA, 2),
    GOLD("Gold", BASE, VIKA, 3),
    ENTERPRISE("Enterprise", BASE, VIKA, 4),
    API_USAGE("ApiUsage", ADD_ON, VIKA, 0),
    CAPACITY("Capacity", ADD_ON, VIKA, 0),
    DINGTALK_BASE("Dingtalk_Base", BASE, DINGTALK, 1),
    DINGTALK_STANDARD("Dingtalk_Standard", BASE, DINGTALK, 2),
    DINGTALK_PROFESSION("Dingtalk_Profession", BASE, WECOM, 3),
    DINGTALK_ENTERPRISE("Dingtalk_Enterprise", BASE, WECOM, 4),
    FEISHU_BASE("Feishu_Base", BASE, LARK, 1),
    FEISHU_STANDARD("Feishu_Standard", BASE, LARK, 2),
    FEISHU_PROFESSION("Feishu_Profession", BASE, WECOM, 3),
    FEISHU_ENTERPRISE("Feishu_Enterprise", BASE, WECOM, 4),
    WECOM_BASE("Wecom_Base", BASE, WECOM, 1),
    WECOM_STANDARD("Wecom_Standard", BASE, WECOM, 2),
    WECOM_PROFESSION("Wecom_Profession", BASE, WECOM, 3),
    WECOM_ENTERPRISE("Wecom_Enterprise", BASE, WECOM, 4),
    PRIVATE_CLOUD("Private_Cloud", BASE, WECOM, 0),

    ATLAS("Atlas", BASE, ALIYUN, 0, true);

    private final String name;

    private final ProductCategory category;

    private final ProductChannel channel;

    private final int rank;

    private final boolean free;

    ProductEnum(String name, ProductCategory category, ProductChannel channel, int rank) {
        this(name, category, channel, rank, false);
    }

    ProductEnum(String name, ProductCategory category, ProductChannel channel, int rank,
                boolean free) {
        this.name = name;
        this.category = category;
        this.channel = channel;
        this.rank = rank;
        this.free = free;
    }

    /**
     * transform by product name.
     *
     * @param name product name
     * @return ProductEnum
     */
    public static ProductEnum of(String name) {
        for (ProductEnum value : ProductEnum.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
