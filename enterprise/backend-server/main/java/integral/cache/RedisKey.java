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

package com.apitable.enterprise.integral.cache;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

public class RedisKey {

    /**
     * invite registration record key value
     */
    public static final String INVITE_HISTORY_KEY = "vikadata:cache:invite:history:{}";

    /**
     * Get notification message lock
     *
     * @param recordId invite history id
     * @return String
     */
    public static String getInviteHistoryKey(String recordId) {
        Assert.notBlank(recordId, "records are not allowed to be empty");
        return StrUtil.format(INVITE_HISTORY_KEY, recordId);
    }
}
