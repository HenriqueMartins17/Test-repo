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

package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OneAccess UserInfo Response")
public class OneAccessUserInfoResponse {

    private String error_code;

    private String msg;

    /**
     * The corresponding login user name
     */
    private String loginName;

    /**
     * The corresponding nickName
     */
    private String nickName;

    /**
     * The corresponding integrated application system account (used when the application account is inconsistent with the user name or has multiple accounts)
     */
    private String[] spRoleList;

    private String uid;

    /**
     * external avatar
     */
    private String avatarUrl;
}
