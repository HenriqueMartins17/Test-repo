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

package com.apitable.enterprise.vika.core;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;

public class Utils {

    public static String getRelativePath(String attachCellValue) {
        return getRelativePath(attachCellValue, null);
    }

    public static String getRelativePath(String attachCellValue, String defaultValue) {
        String matchStr = ReUtil.getGroup1("\\((.+)\\)", attachCellValue);
        if (matchStr == null) {
            return defaultValue;
        }
        return StrUtil.removePrefix(URLUtil.getPath(matchStr), "/");
    }
}
