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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.ops.ro.SyncSocialDingTalkAppRo;
import com.apitable.enterprise.social.entity.SocialDingtalkAppEntity;
import com.apitable.enterprise.social.mapper.SocialDingTalkAppMapper;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.ISocialDingTalkAppService;
import com.vikadata.social.dingtalk.DingtalkConfig;
import com.vikadata.social.dingtalk.DingtalkConfig.AgentApp;
import com.vikadata.social.dingtalk.config.DingTalkConfigStorage;

import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SocialDingTalkAppServiceImpl
    extends ServiceImpl<SocialDingTalkAppMapper, SocialDingtalkAppEntity>
    implements ISocialDingTalkAppService {
    @Resource
    private SocialDingTalkAppMapper socialDingTalkAppMapper;

    @Resource
    private IDingTalkService iDingTalkService;

    @Override
    public void syncAppConfigInStorage(List<SyncSocialDingTalkAppRo> roList) {
        if (CollUtil.isEmpty(roList)) {
            return;
        }
        DingtalkConfig dingtalkConfig = iDingTalkService.getDingTalkConfig();
        if (null != dingtalkConfig && null != dingtalkConfig.getAgentAppStorage()) {
            DingTalkConfigStorage configStorage = dingtalkConfig.getAgentAppStorage();
            roList.forEach(ro -> {
                AgentApp agentApp = new AgentApp();
                agentApp.setAgentId(ro.getAgentId());
                agentApp.setCustomKey(ro.getSuiteId());
                agentApp.setCustomSecret(ro.getSuiteSecret());
                agentApp.setToken(ro.getToken());
                agentApp.setAesKey(ro.getAesKey());
                agentApp.setCorpId(ro.getAuthCorpId());
                agentApp.setSuiteTicket(ro.getSuiteTicket());
                configStorage.setAgentApp(agentApp);
            });
        }
    }

    @Override
    public void createOrUpdateBatch(Long userId, List<SyncSocialDingTalkAppRo> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }
        List<SocialDingtalkAppEntity> entities = new ArrayList<>();
        dataList.forEach(i -> entities.add(SocialDingtalkAppEntity.builder()
            .id(IdWorker.getId())
            .suiteId(i.getSuiteId())
            .createdBy(userId)
            .updatedBy(userId)
            .appType(i.getAppType())
            .suiteSecret(i.getSuiteSecret())
            .token(i.getToken())
            .aesKey(i.getAesKey())
            .suiteTicket(i.getSuiteTicket())
            .authCorpId(i.getAuthCorpId())
            .agentId(i.getAgentId())
            .build()));
        socialDingTalkAppMapper.insertOrUpdateBatch(entities);
        syncAppConfigInStorage(dataList);
    }

}
