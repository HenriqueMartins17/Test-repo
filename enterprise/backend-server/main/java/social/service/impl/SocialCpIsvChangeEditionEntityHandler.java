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

import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialEditionChangelogWeComService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.vikabilling.util.WeComPlanConfigManager;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.entity.SocialEditionChangelogWecomEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.event.order.WeComOrderPaidEvent;
import com.vikadata.social.wecom.model.WxCpIsvAuthInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application version change processing
 * </p>
 */
@Service
public class SocialCpIsvChangeEditionEntityHandler implements ISocialCpIsvEntityHandler {

    @Resource
    private ISocialEditionChangelogWeComService socialEditionChangelogWeComService;

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Override
    public WeComIsvMessageType type() {

        return WeComIsvMessageType.CHANGE_EDITION;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {
        List<String> spaceIds =
            socialTenantBindService.getSpaceIdsByTenantIdAndAppId(unprocessed.getAuthCorpId(),
                unprocessed.getSuiteId());
        if (CollUtil.isNotEmpty(spaceIds)) {
            // Tenant space station does not exist, so it will not be processed
            // If the enterprise installation is paid at the same time, it will be handled by Authorized Installation
            String suiteId = unprocessed.getSuiteId();
            String authCorpId = unprocessed.getAuthCorpId();
            // 1 Get the last version information
            SocialEditionChangelogWecomEntity lastChangelog = socialEditionChangelogWeComService
                .getLastChangeLog(suiteId, authCorpId);
            // 2 Save this version information
            SocialEditionChangelogWecomEntity changelogWecomEntity =
                socialEditionChangelogWeComService
                    .createChangelog(suiteId, authCorpId);
            // 3 Process Trial Subscription
            WxCpIsvAuthInfo.EditionInfo.Agent agent =
                JSONUtil.toBean(changelogWecomEntity.getEditionInfo(),
                    WxCpIsvAuthInfo.EditionInfo.Agent.class);
            String editionId = agent.getEditionId();
            if (WeComPlanConfigManager.isWeComTrialEdition(editionId)) {
                WeComOrderPaidEvent event =
                    SocialFactory.formatWecomTailEditionOrderPaidEvent(suiteId, authCorpId,
                        ClockManager.me().getLocalDateTimeNow(), agent);
                String orderId = SocialOrderStrategyFactory.getService(SocialPlatformType.WECOM)
                    .retrieveOrderPaidEvent(event);
                // Synchronize order events
                if (orderId != null) {
                    SpringContextHolder.getApplicationContext()
                        .publishEvent(new SyncOrderEvent(this, orderId));
                }
            }
        }
        return true;
    }

}
