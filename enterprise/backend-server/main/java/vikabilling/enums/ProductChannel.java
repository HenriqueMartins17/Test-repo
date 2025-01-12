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
 * product channel
 * @author Shawn Deng
 */
@Getter
@RequiredArgsConstructor
public enum ProductChannel {

     PRIVATE("private"),
     VIKA("vika"),
     DINGTALK("dingtalk"),
     LARK("lark"),
     WECOM("wecom"),
     ALIYUN("aliyun");

    private final String name;

    public static ProductChannel of(String name) {
        for (ProductChannel value : ProductChannel.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
