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

import jakarta.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialFeishuEventLogMapper;
import com.apitable.enterprise.social.service.ISocialFeishuEventLogService;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.social.entity.SocialFeishuEventLogEntity;
import com.vikadata.social.feishu.event.BaseEvent;
import com.vikadata.social.feishu.event.contact.v3.BaseV3ContactEvent;

import org.springframework.stereotype.Service;

/**
 * Third party platform integration - implementation of the Lark event log service interface
 */
@Service
@Slf4j
public class SocialFeishuEventLogServiceImpl
    extends ServiceImpl<SocialFeishuEventLogMapper, SocialFeishuEventLogEntity>
    implements ISocialFeishuEventLogService {

    @Resource
    private SocialFeishuEventLogMapper socialFeishuEventLogMapper;

    @Override
    public void doneEvent(String uuid) {
        if (StrUtil.isBlank(uuid)) {
            return;
        }
        socialFeishuEventLogMapper.updateStatusTrueByUuid(uuid);
    }

    @Override
    public <T extends BaseEvent> boolean create(T event) {
        boolean duplicate = SqlTool.retCount(
            socialFeishuEventLogMapper.selectCountByUuid(event.getMeta().getUuid())) > 0;
        if (duplicate) {
            log.error(
                "Repeat Lark event notification. Please check whether it has been processed. Tenant[{}], event type[{}], event id[{}]",
                event.getTenantKey(), event.getType(), event.getMeta().getUuid());
            return false;
        }
        SocialFeishuEventLogEntity eventLog = new SocialFeishuEventLogEntity();
        eventLog.setTs(event.getMeta().getTs());
        eventLog.setUuid(event.getMeta().getUuid());
        eventLog.setType(event.getType());
        eventLog.setAppId(event.getAppId());
        eventLog.setTenantKey(event.getTenantKey());
        eventLog.setEventData(JSONUtil.toJsonStr(event));
        return save(eventLog);
    }

    @Override
    public <T extends BaseV3ContactEvent> boolean createV3ContactEventLog(T event) {
        SocialFeishuEventLogEntity logEntity =
            socialFeishuEventLogMapper.selectByUuid(event.getHeader().getEventId());
        if (logEntity != null) {
            log.error(
                "Repeat Lark event notification. Please check whether it has been processed. Tenant[{}],event type[{}],event id[{}]",
                event.getHeader().getTenantKey(), event.getHeader().getEventType(),
                event.getHeader().getEventId());
            return logEntity.getStatus();
        }
        SocialFeishuEventLogEntity eventLog = new SocialFeishuEventLogEntity();
        eventLog.setTs(event.getHeader().getCreateTime());
        eventLog.setUuid(event.getHeader().getEventId());
        eventLog.setType(event.getHeader().getEventType());
        eventLog.setAppId(event.getHeader().getAppId());
        eventLog.setTenantKey(event.getHeader().getTenantKey());
        eventLog.setEventData(JSONUtil.toJsonStr(event));
        return save(eventLog);
    }
}
