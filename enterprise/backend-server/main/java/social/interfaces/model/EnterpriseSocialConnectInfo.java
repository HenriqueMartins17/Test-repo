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

package com.apitable.enterprise.social.interfaces.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.util.StrUtil;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.remind.IMDingtalkRemind;
import com.apitable.enterprise.social.remind.IMFeishuRemind;
import com.apitable.enterprise.social.remind.IMWecomRemind;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.interfaces.social.model.SocialConnectInfo;
import com.apitable.space.enums.SpaceResourceGroupCode;

public class EnterpriseSocialConnectInfo implements SocialConnectInfo {

    private String spaceId;

    private SocialPlatformType platformType;

    private SocialAppType appType;

    private String appId;

    private String tenantId;

    private SocialTenantAuthMode authMode;

    private boolean enabled;

    private boolean contactSyncing;

    private String remindObserverClassName;

    private List<SpaceResourceGroupCode> disableResourceGroupCodes = new ArrayList<>();

    public EnterpriseSocialConnectInfo(String spaceId, SocialPlatformType platformType, SocialAppType appType, String appId, String tenantId, SocialTenantAuthMode authMode, boolean enabled, boolean contactSyncing) {
        this.spaceId = spaceId;
        this.platformType = platformType;
        this.appType = appType;
        this.appId = appId;
        this.tenantId = tenantId;
        this.authMode = authMode;
        this.enabled = enabled;
        this.contactSyncing = contactSyncing;
        initRemindObserverClassName();
        initDisableGroupCodes();
    }

    private void initRemindObserverClassName() {
        if (platformType == SocialPlatformType.FEISHU) {
            remindObserverClassName = StrUtil.lowerFirst(IMFeishuRemind.class.getSimpleName());
        }
        else if (platformType == SocialPlatformType.DINGTALK) {
            remindObserverClassName = StrUtil.lowerFirst(IMDingtalkRemind.class.getSimpleName());
        }
        else if (platformType == SocialPlatformType.WECOM) {
            remindObserverClassName = StrUtil.lowerFirst(IMWecomRemind.class.getSimpleName());
        }
    }

    private void initDisableGroupCodes() {
        boolean isDingTalkIsv = SocialPlatformType.DINGTALK == platformType &&
                SocialAppType.ISV == appType;
        boolean isWeComIsv = SocialPlatformType.WECOM == platformType &&
                SocialAppType.ISV == appType;
        if (isDingTalkIsv || isWeComIsv) {
            disableResourceGroupCodes = new ArrayList<>();
        }
        boolean isLarkIsv = SocialPlatformType.FEISHU == platformType &&
                SocialAppType.ISV == appType;
        if (isLarkIsv) {
            disableResourceGroupCodes = Stream.of("MANAGE_NORMAL_MEMBER", "MANAGE_TEAM").map(SpaceResourceGroupCode::valueOf).collect(Collectors.toList());
        }
    }

    @Override
    public String getSpaceId() {
        return spaceId;
    }

    @Override
    public Integer getPlatform() {
        return platformType.getValue();
    }

    @Override
    public Integer getAppType() {
        return appType.getType();
    }

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public Integer getAuthMode() {
        return authMode.getValue();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean contactSyncing() {
        return false;
    }

    @Override
    public String getRemindObserver() {
        return remindObserverClassName;
    }

    @Override
    public List<SpaceResourceGroupCode> getDisableResourceGroupCodes() {
        return disableResourceGroupCodes;
    }
}
