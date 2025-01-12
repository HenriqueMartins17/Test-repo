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

import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.SOCIAL_ISV_EVENT_EXCHANGE;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.SOCIAL_ISV_WECOM_ROUTING_KEY;

import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.mapper.SocialCpIsvMessageMapper;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.starter.amqp.core.RabbitSenderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application message notification information
 * </p>
 */
@Service
public class SocialCpIsvMessageServiceImpl
    extends ServiceImpl<SocialCpIsvMessageMapper, SocialCpIsvEventLogEntity>
    implements ISocialCpIsvMessageService, InitializingBean {

    private static final Map<WeComIsvMessageType, ISocialCpIsvEntityHandler> ENTITY_HANDLERS =
        Maps.newHashMapWithExpectedSize(16);

    private static final String SQL_LIMIT = "LIMIT %d";

    @Resource
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @Override
    public List<SocialCpIsvEventLogEntity> getUnprocessedList(int size) {
        LambdaQueryWrapper<SocialCpIsvEventLogEntity> queryWrapper =
            Wrappers.lambdaQuery(SocialCpIsvEventLogEntity.class)
                .between(SocialCpIsvEventLogEntity::getProcessStatus,
                    SocialCpIsvMessageProcessStatus.PENDING.getValue(),
                    SocialCpIsvMessageProcessStatus.REJECT_TEMPORARILY.getValue())
                .orderByAsc(SocialCpIsvEventLogEntity::getTimestamp)
                .last(String.format(SQL_LIMIT, size));
        return list(queryWrapper);
    }

    @Override
    public void doUnprocessedInfo(SocialCpIsvEventLogEntity unprocessedInfo)
        throws WxErrorException {
        ENTITY_HANDLERS.get(WeComIsvMessageType.fromType(unprocessedInfo.getType()))
            .process(unprocessedInfo);
    }

    @Override
    public void sendToMq(Long unprocessedId, String infoType, String authCorpId, String suiteId) {
        rabbitSenderService.topicSend(SOCIAL_ISV_EVENT_EXCHANGE, SOCIAL_ISV_WECOM_ROUTING_KEY,
            unprocessedId.toString(), SocialCpIsvEventLogEntity.builder()
                .id(unprocessedId)
                .suiteId(suiteId)
                .infoType(infoType)
                .authCorpId(authCorpId)
                .build());
    }

    @Override
    public void updateStatusById(Long id, SocialCpIsvMessageProcessStatus status) {
        baseMapper.updateStatusById(id, status.getValue());
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(ISocialCpIsvEntityHandler.class).values().
            forEach(handler -> ENTITY_HANDLERS.put(handler.type(), handler));
    }

}
