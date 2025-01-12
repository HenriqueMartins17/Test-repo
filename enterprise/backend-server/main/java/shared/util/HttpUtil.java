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

package com.apitable.enterprise.shared.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;

public class HttpUtil {

    private static final String VIKA_DESKTOP = "VikaDesktop";

    public static String getVikaDesktop(String userAgent, boolean browser) {
        if (StrUtil.isBlank(userAgent)) {
            return null;
        }

        UserAgent ua = UserAgentUtil.parse(userAgent);
        // return client information first
        if (StrUtil.containsIgnoreCase(userAgent, VIKA_DESKTOP)) {
            int start = StrUtil.indexOfIgnoreCase(userAgent, VIKA_DESKTOP);
            return StrUtil.subBefore(userAgent.substring(start), ' ', false) +
                StrUtil.format(" ({})", ua.getPlatform());
        }
        // otherwise return the platform type
        StringBuilder platform = new StringBuilder(ua.getPlatform().toString());
        if (browser && !ua.getBrowser().equals(Browser.Unknown)) {
            platform.append(" ").append(ua.getBrowser().toString());
        }
        return platform.toString();
    }
}
