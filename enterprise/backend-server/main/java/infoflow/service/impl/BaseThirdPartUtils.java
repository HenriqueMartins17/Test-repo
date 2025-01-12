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

package com.apitable.enterprise.infoflow.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class BaseThirdPartUtils {
    /**
     * sign param for infoflow
     *
     * @param paramMap request paramMap
     * @param sk       appSecret
     * @return sign string
     */
    protected String infoflowSign(Map<String, Object> paramMap, String sk) {
        // sort
        Map<String, Object> params = new TreeMap<>(paramMap);
        // en-code
        String query = HttpUtil.toParams(params, StandardCharsets.UTF_8);
        // encode
        String md5 = SecureUtil.md5(query);
        // salt
        String data = md5 + sk;
        // encode
        String sha256 = SecureUtil.sha256(data);
        return sha256;
    }
}
