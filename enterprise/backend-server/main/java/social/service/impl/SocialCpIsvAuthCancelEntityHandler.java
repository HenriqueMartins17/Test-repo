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

package com.apitable.enterprise.social.service.impl;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import cn.hutool.core.lang.Assert;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider cancels authorization processing
 * </p>
 */
@Service
public class SocialCpIsvAuthCancelEntityHandler
    implements ISocialCpIsvEntityHandler, InitializingBean {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private ISocialTenantService socialTenantService;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType type() {

        return WeComIsvMessageType.AUTH_CANCEL;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {

        String suiteId = unprocessed.getSuiteId();
        String authCorpId = unprocessed.getAuthCorpId();

        // 1 Obtain the existing tenant information of the enterprise
        SocialTenantEntity socialTenantEntity =
            socialTenantService.getByAppIdAndTenantId(suiteId, authCorpId);
        Assert.notNull(socialTenantEntity, () -> new IllegalStateException(String
            .format("No available tenant information found,tenantId：%s，appId：%s", authCorpId,
                suiteId)));

        // 2 Unbind the app market
        String spaceId =
            socialTenantBindService.getTenantBindSpaceId(socialTenantEntity.getTenantId(),
                socialTenantEntity.getAppId());
        iAppInstanceService.deleteBySpaceIdAndAppType(spaceId, AppType.WECOM_STORE.name());
        // 3 Deactivate
        socialTenantEntity.setStatus(false);
        socialTenantEntity.setUpdatedAt(LocalDateTime.now());
        socialTenantService.updateById(socialTenantEntity);
        // 4 Clear the space station cache
        userSpaceCacheService.delete(spaceId);
        return true;

    }

    @Override
    public void afterPropertiesSet() {

        this.socialCpIsvMessageService =
            applicationContext.getBean(ISocialCpIsvMessageService.class);

    }

}
