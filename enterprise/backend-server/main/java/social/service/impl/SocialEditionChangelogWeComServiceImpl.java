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

import java.util.Optional;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.social.mapper.SocialEditionChangelogWeComMapper;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.service.ISocialEditionChangelogWeComService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialEditionChangelogWecomEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.WxCpIsvServiceImpl;
import com.vikadata.social.wecom.model.WxCpIsvAuthInfo;
import com.vikadata.social.wecom.model.WxCpIsvAuthInfo.EditionInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application version change information
 * </p>
 */
@Service
public class SocialEditionChangelogWeComServiceImpl
    extends ServiceImpl<SocialEditionChangelogWeComMapper, SocialEditionChangelogWecomEntity>
    implements ISocialEditionChangelogWeComService {

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    @Resource
    private ISocialTenantService socialTenantService;

    @Override
    public SocialEditionChangelogWecomEntity createChangelog(String suiteId, String paidCorpId)
        throws WxErrorException {
        return createChangelog(suiteId, paidCorpId, true);
    }

    @Override
    public SocialEditionChangelogWecomEntity createChangelog(String suiteId, String paidCorpId,
                                                             boolean fetchEditionInfo)
        throws WxErrorException {
        SocialEditionChangelogWecomEntity entity = SocialEditionChangelogWecomEntity.builder()
            .suiteId(suiteId)
            .paidCorpId(paidCorpId)
            .build();
        if (fetchEditionInfo) {
            // Need to get the enterprise application version information
            SocialTenantEntity socialTenantEntity =
                socialTenantService.getByAppIdAndTenantId(suiteId, paidCorpId);
            ExceptionUtil.isNotNull(socialTenantEntity, SocialException.TENANT_NOT_EXIST);
            ExceptionUtil.isTrue(socialTenantEntity.getStatus(), SocialException.TENANT_DISABLED);
            // Get and fill in the application version information
            WxCpIsvServiceImpl wxCpIsvService =
                (WxCpIsvServiceImpl) weComTemplate.isvService(suiteId);
            WxCpIsvAuthInfo wxCpIsvAuthInfo =
                wxCpIsvService.getAuthInfo(paidCorpId, socialTenantEntity.getPermanentCode());
            WxCpIsvAuthInfo.EditionInfo.Agent agent =
                Optional.ofNullable(wxCpIsvAuthInfo.getEditionInfo())
                    .map(WxCpIsvAuthInfo.EditionInfo::getAgents)
                    .filter(agents -> !agents.isEmpty())
                    .map(agents -> agents.get(0))
                    .orElse(null);
            entity.setEditionInfo(JSONUtil.toJsonStr(agent));
        }
        save(entity);
        return entity;
    }

    @Override
    public SocialEditionChangelogWecomEntity createChangelog(String suiteId, String paidCorpId,
                                                             EditionInfo.Agent editionInfoAgent) {
        SocialEditionChangelogWecomEntity entity = SocialEditionChangelogWecomEntity.builder()
            .suiteId(suiteId)
            .paidCorpId(paidCorpId)
            .editionInfo(JSONUtil.toJsonStr(editionInfoAgent))
            .build();
        save(entity);
        return entity;
    }

    @Override
    public SocialEditionChangelogWecomEntity getLastChangeLog(String suiteId, String paidCorpId) {
        return getBaseMapper().selectLastChangeLog(suiteId, paidCorpId);
    }

}
