/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.model;

import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;

/**
 * User.
 */
public interface User {

    String getAreaCode();

    String getTelephoneNumber();

    String getEmailAddress();

    String getPassword();

    String getNickName();

    String getAvatar();

    String getAppId();

    String getTenantId();

    String getOpenId();

    String getUnionId();

    SocialPlatformType getSocialPlatformType();

    SocialAppType getSocialAppType();

}
