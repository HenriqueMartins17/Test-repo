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

package com.apitable.enterprise.appstore.factory;

import cn.hutool.http.HttpUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.social.constants.LarkConstants;
import com.apitable.shared.config.properties.ConstProperties;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lark Application Configuration Factory
 */
public class LarkConfigFactory {

    private static final String baseUrl = "https://open.feishu.cn/open-apis/authen/v1/index";

    public static String createRedirectUri(String appInstanceId) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        return constProperties.getServerDomain() +
            LarkConstants.formatInternalLoginUrl(appInstanceId);
    }

    public static String createAuthUrl(String appId, String redirectUri) {
        Map<String, Object> queryMap = new HashMap<>(2);
        queryMap.put("app_id", appId);
        queryMap.put("redirect_uri", redirectUri);
        return HttpUtil.urlWithForm(baseUrl, queryMap, StandardCharsets.UTF_8, false);
    }

    public static String createEventUri(String appInstanceId) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        return constProperties.getServerDomain() +
            LarkConstants.formatInternalEventUrl(appInstanceId);
    }
}
