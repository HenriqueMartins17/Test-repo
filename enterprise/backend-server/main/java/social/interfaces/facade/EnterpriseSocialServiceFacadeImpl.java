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

package com.apitable.enterprise.social.interfaces.facade;

import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.interfaces.model.EnterpriseSocialConnectInfo;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.notification.SocialNotificationManagement;
import com.apitable.enterprise.social.service.IDingTalkDaService;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDomainService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.interfaces.social.event.CallEventType;
import com.apitable.interfaces.social.event.NotificationEvent;
import com.apitable.interfaces.social.event.SocialEvent;
import com.apitable.interfaces.social.event.TemplateQuoteEvent;
import com.apitable.interfaces.social.facade.SocialServiceFacade;
import com.apitable.interfaces.social.model.SocialConnectInfo;
import com.apitable.interfaces.social.model.SocialUserBind;
import com.apitable.space.enums.SpaceUpdateOperate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Agent;
import me.chanjar.weixin.cp.bean.WxCpTpContactSearchResp;

/**
 * social facade implements.
 */
@Slf4j
public class EnterpriseSocialServiceFacadeImpl implements SocialServiceFacade {

    private final ISocialTenantService iSocialTenantService;

    private final ISocialTenantBindService iSocialTenantBindService;

    private final ISocialUserBindService iSocialUserBindService;

    private final ISocialService iSocialService;

    private final IDingTalkDaService iDingTalkDaService;

    private final IDingTalkInternalService iDingTalkInternalService;

    private final ISocialTenantDomainService iSocialTenantDomainService;

    private final ISocialCpIsvService iSocialCpIsvService;

    /**
     * construct.
     */
    public EnterpriseSocialServiceFacadeImpl(ISocialTenantService socialTenantService,
                                             ISocialTenantBindService socialTenantBindService,
                                             ISocialUserBindService socialUserBindService,
                                             ISocialService socialService,
                                             IDingTalkDaService dingTalkDaService,
                                             IDingTalkInternalService dingTalkInternalService,
                                             ISocialTenantDomainService socialTenantDomainService,
                                             ISocialCpIsvService socialCpIsvService) {
        this.iSocialTenantService = socialTenantService;
        this.iSocialTenantBindService = socialTenantBindService;
        this.iSocialUserBindService = socialUserBindService;
        this.iSocialService = socialService;
        this.iDingTalkDaService = dingTalkDaService;
        this.iDingTalkInternalService = dingTalkInternalService;
        this.iSocialTenantDomainService = socialTenantDomainService;
        this.iSocialCpIsvService = socialCpIsvService;
    }

    @Override
    public void createSocialUser(SocialUserBind socialUser) {
        boolean isExist =
            iSocialUserBindService.isUnionIdBind(socialUser.getUserId(), socialUser.getUnionId());
        if (!isExist) {
            iSocialUserBindService.create(socialUser.getUserId(), socialUser.getUnionId());
        }
    }

    @Override
    public Long getUserIdByUnionId(String unionId) {
        return iSocialUserBindService.getUserIdByUnionId(unionId);
    }

    @Override
    public String getSpaceIdByDomainName(String domainName) {
        return iSocialTenantDomainService.getSpaceIdByDomainName(domainName);
    }

    @Override
    public String getDomainNameBySpaceId(String spaceId, boolean appendHttpsPrefix) {
        return iSocialTenantDomainService.getDomainNameBySpaceId(spaceId, false);
    }

    @Override
    public Map<String, String> getDomainNameMap(List<String> spaceIds) {
        return iSocialTenantDomainService.getSpaceDomainBySpaceIdsToMap(spaceIds);
    }

    @Override
    public void removeDomainBySpaceIds(List<String> spaceIds) {
        iSocialTenantDomainService.removeDomain(spaceIds);
    }

    @Override
    public SocialConnectInfo getConnectInfo(String spaceId) {
        TenantBindDTO tenantBindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (tenantBindInfo != null && tenantBindInfo.getAppId() != null) {
            SocialTenantEntity socialTenant =
                iSocialTenantService.getByAppIdAndTenantId(tenantBindInfo.getAppId(),
                    tenantBindInfo.getTenantId());
            if (socialTenant != null) {
                boolean contactSyncing = iSocialService.isContactSyncing(spaceId);
                return new EnterpriseSocialConnectInfo(spaceId,
                    SocialPlatformType.toEnum(socialTenant.getPlatform()),
                    SocialAppType.of(socialTenant.getAppType()), socialTenant.getAppId(),
                    socialTenant.getTenantId(),
                    SocialTenantAuthMode.fromTenant(socialTenant.getAuthMode()),
                    socialTenant.getStatus(), contactSyncing);
            }
        }
        return null;
    }

    @Override
    public boolean checkSocialBind(String spaceId) {
        return iSocialTenantBindService.getSpaceBindStatus(spaceId);
    }

    @Override
    public void checkCanOperateSpaceUpdate(String spaceId, SpaceUpdateOperate spaceUpdateOperate) {
        iSocialService.checkCanOperateSpaceUpdate(spaceId, spaceUpdateOperate);
    }

    @Override
    public void checkWhetherSpaceCanChangeMainAdmin(String spaceId, Long opMemberId,
                                                    Long acceptMemberId,
                                                    List<SpaceUpdateOperate> spaceUpdateOperates) {
        iSocialService.checkCanOperateSpaceUpdate(spaceId, opMemberId, acceptMemberId,
            spaceUpdateOperates);
    }

    @Override
    public void deleteUser(Long userId) {
        iSocialService.deleteSocialUserBind(userId);
    }

    @Override
    public String getSuiteKeyByDingtalkSuiteId(String suiteId) {
        IsvAppProperty appConfig = iDingTalkInternalService.getIsvAppConfig(suiteId);
        return appConfig != null ? appConfig.getSuiteKey() : null;
    }

    @Override
    public List<String> fuzzySearchIfSatisfyCondition(String spaceId, String word) {
        SocialTenantBindEntity bindEntity = iSocialTenantBindService.getBySpaceId(spaceId);
        SocialTenantEntity socialTenantEntity = Optional.ofNullable(bindEntity)
            .map(bind -> iSocialTenantService
                .getByAppIdAndTenantId(bind.getAppId(), bind.getTenantId()))
            .orElse(null);
        if (Objects.nonNull(socialTenantEntity)
            && SocialPlatformType.WECOM.getValue().equals(socialTenantEntity.getPlatform())
            && SocialAppType.ISV.getType() == socialTenantEntity.getAppType()) {
            // If it is the space bound to the wecom, it is necessary to query the qualified users in the wecom contacts.
            String suiteId = socialTenantEntity.getAppId();
            String authCorpId = socialTenantEntity.getTenantId();
            Agent agent = JSONUtil.toBean(socialTenantEntity.getContactAuthScope(), Agent.class);
            Integer agentId = agent.getAgentId();
            WxCpTpContactSearchResp.QueryResult queryResult = null;
            try {
                queryResult = iSocialCpIsvService.search(suiteId, authCorpId, agentId, word, 1);
            } catch (WxErrorException e) {
                log.error("Failed to search users from wecom isv.", e);
            }
            if (queryResult != null) {
                return queryResult.getUser().getUserid();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends SocialEvent> void eventCall(T event) {
        if (event.getEventType() == CallEventType.TEMPLATE_QUOTE) {
            TemplateQuoteEvent quoteEvent = (TemplateQuoteEvent) event;
            iDingTalkDaService.handleTemplateQuoted(quoteEvent.getSpaceId(), quoteEvent.getNodeId(),
                quoteEvent.getTemplateId(), quoteEvent.getMemberId());
        } else if (event.getEventType() == CallEventType.NOTIFICATION) {
            NotificationEvent notificationEvent = (NotificationEvent) event;
            SocialNotificationManagement.me().socialNotify(notificationEvent.getNotificationMeta());
        }
    }
}
