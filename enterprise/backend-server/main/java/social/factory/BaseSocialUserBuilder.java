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

package com.apitable.enterprise.social.factory;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.model.SocialUser;

/**
 * <p>
 * Household Foundation User Builder
 * </p>
 */
public class BaseSocialUserBuilder<T> {

    protected String nickName;

    protected String avatar;

    protected String tenantId;

    protected String openId;

    protected String unionId;

    protected SocialPlatformType socialPlatformType;

    protected SocialAppType socialAppType;

    public T nickName(String nickName) {
        this.nickName = nickName;
        return (T) this;
    }

    public T avatar(String avatar) {
        this.avatar = avatar;
        return (T) this;
    }

    public T tenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

    public T openId(String openId) {
        this.openId = openId;
        return (T) this;
    }

    public T unionId(String unionId) {
        this.unionId = unionId;
        return (T) this;
    }

    public T socialPlatformType(SocialPlatformType socialPlatformType) {
        this.socialPlatformType = socialPlatformType;
        return (T) this;
    }

    public T socialAppType(SocialAppType socialAppType) {
        this.socialAppType = socialAppType;
        return (T) this;
    }

    public SocialUser build() {
        SocialUser su = new SocialUser();
        su.setNickName(this.nickName);
        su.setAvatar(this.avatar);
        su.setTenantId(this.tenantId);
        su.setOpenId(this.openId);
        su.setUnionId(this.unionId);
        su.setSocialPlatformType(this.socialPlatformType);
        su.setSocialAppType(this.socialAppType);
        return su;
    }

}
