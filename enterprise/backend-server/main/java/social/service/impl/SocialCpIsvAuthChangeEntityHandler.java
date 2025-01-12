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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.Resource;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.shared.exception.LimitException;
import com.apitable.space.mapper.SpaceMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Agent;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.AuthCorpInfo;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.AuthInfo;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Privilege;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;

import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.event.wecom.WeComIsvCardFactory;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider change authorization processing
 * </p>
 */
@Slf4j
@Service
public class SocialCpIsvAuthChangeEntityHandler
    implements ISocialCpIsvEntityHandler, InitializingBean {

    @Resource
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    @Resource
    private ISocialCpIsvService socialCpIsvService;

    @Resource
    private ISocialCpIsvPermitService socialCpIsvPermitService;

    @Resource
    private ISocialTenantService socialTenantService;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;


    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType type() {

        return WeComIsvMessageType.AUTH_CHANGE;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {

        String suiteId = unprocessed.getSuiteId();
        String authCorpId = unprocessed.getAuthCorpId();

        // 1 Obtain the existing tenants and space station information of the enterprise
        SocialTenantEntity socialTenantEntity =
            socialTenantService.getByAppIdAndTenantId(suiteId, authCorpId);
        Assert.notNull(socialTenantEntity, () -> new IllegalStateException(String
            .format("No available tenant information found,tenantId：%s，appId：%s", authCorpId,
                suiteId)));
        String spaceId = socialTenantBindService.getTenantBindSpaceId(authCorpId, suiteId);
        Assert.notBlank(spaceId, () -> new IllegalStateException(String
            .format("No corresponding space station information was found,tenantId：%s，appId：%s",
                authCorpId, suiteId)));
        // 2 Get the latest authorization information of the enterprise
        WxCpTpAuthInfo wxCpTpAuthInfo = weComTemplate.isvService(suiteId)
            .getAuthInfo(authCorpId, socialTenantEntity.getPermanentCode());
        AuthCorpInfo authCorpInfo = wxCpTpAuthInfo.getAuthCorpInfo();
        Agent agent = Optional.ofNullable(wxCpTpAuthInfo.getAuthInfo())
            .map(AuthInfo::getAgents)
            .filter(agents -> !agents.isEmpty())
            .map(agents -> agents.get(0))
            .orElse(null);
        Objects.requireNonNull(authCorpInfo, "AuthCorpInfo cannot be null.");
        Objects.requireNonNull(agent, "Agent cannot be null.");
        // 3 Update the authorization information of the enterprise
        socialTenantEntity.setContactAuthScope(JSONUtil.toJsonStr(agent));
        socialTenantEntity.setAuthMode(
            SocialTenantAuthMode.fromWeCom(agent.getAuthMode()).getValue());
        socialTenantEntity.setAuthInfo(JSONUtil.toJsonStr(wxCpTpAuthInfo));
        socialTenantService.updateById(socialTenantEntity);
        // If necessary, refresh access first_ token
        socialCpIsvService.refreshAccessToken(suiteId, unprocessed.getAuthCorpId(),
            socialTenantEntity.getPermanentCode());
        // 4 Resynchronize contacts
        Privilege privilege = agent.getPrivilege();
        socialCpIsvService.syncViewableUsers(suiteId, authCorpInfo.getCorpId(), spaceId,
            privilege.getAllowUsers(), privilege.getAllowParties(), privilege.getAllowTags(),
            socialTenantEntity, agent.getAgentId());
        // 5 Send the start message to the new member
        WxCpMessage wxCpMessage = WeComIsvCardFactory.createWelcomeMsg(agent.getAgentId());
        socialCpIsvService.sendWelcomeMessage(socialTenantEntity, spaceId, wxCpMessage);
        // 7 Empty temporary cache
        socialCpIsvService.clearCache(authCorpInfo.getCorpId());
        // 8 Clear the space station cache
        userSpaceCacheService.delete(spaceId);
        // 9 Interface license processing
        try {
            socialCpIsvPermitService.autoProcessPermitOrder(suiteId, authCorpId, spaceId);
        } catch (Exception ex) {
            log.error("WeCom interface license automation processing failed", ex);
        }

        return true;

    }

    @Override
    public void afterPropertiesSet() {

        this.socialCpIsvMessageService =
            applicationContext.getBean(ISocialCpIsvMessageService.class);

    }

}
