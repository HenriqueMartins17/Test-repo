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

package com.apitable.enterprise.wechat.cache;

public class RedisKey {

    /**
     * WeChat official account, page authorization callback code
     */
    public static final String WECHAT_MP_CODE_MARK = "vikadata:wechat:mp:code:{}";

    /**
     * WeChat official account, generate the unique identifier of the two-dimensional code
     */
    public static final String WECHAT_MP_QRCODE_MARK = "vikadata:wechat:mp:qrcode:{}";
}
