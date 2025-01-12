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

import lombok.Data;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.factory.SocialFeishuBuilder;
import com.apitable.enterprise.social.factory.SocialWeComBuilder;

@Data
public class SocialUser implements User {

    private String nickName;

    private String avatar;

    private String tenantId;

    private String openId;

    private String unionId;

    private String appId;

    private String areaCode;

    private String telephoneNumber;

    private String emailAddress;

    private SocialPlatformType socialPlatformType;

    private SocialAppType socialAppType;

    public SocialUser() {
    }

    public SocialUser(String nickName, String avatar, String appId, String tenantId, String openId, String unionId, SocialPlatformType socialPlatformType) {
        this.nickName = nickName;
        this.avatar = avatar;
        this.appId = appId;
        this.tenantId = tenantId;
        this.openId = openId;
        this.unionId = unionId;
        this.socialPlatformType = socialPlatformType;
    }

    public SocialUser(String nickName, String avatar,
            String areaCode, String telephoneNumber, String emailAddress,
            String appId, String tenantId, String openId, String unionId, SocialPlatformType socialPlatformType) {
        this.nickName = nickName;
        this.avatar = avatar;
        this.areaCode = areaCode;
        this.telephoneNumber = telephoneNumber;
        this.emailAddress = emailAddress;
        this.appId = appId;
        this.tenantId = tenantId;
        this.openId = openId;
        this.unionId = unionId;
        this.socialPlatformType = socialPlatformType;
    }

    /**
     * Lark Builder
     */
    public static SocialFeishuBuilder FEISHU() {
        return new SocialFeishuBuilder();
    }

    /**
     * WeCom Builder
     */
    public static SocialWeComBuilder WECOM() {
        return new SocialWeComBuilder();
    }

    @Override
    public String getPassword() {
        return null;
    }

}
