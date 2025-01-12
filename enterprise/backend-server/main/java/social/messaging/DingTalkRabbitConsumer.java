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

package com.apitable.enterprise.social.messaging;

import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_ISV_HIGH_TOPIC;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_ISV_TOPIC_QUEUE_NAME_DEAD;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_TOPIC_EXCHANGE_BUFFER;

import com.apitable.enterprise.social.service.IDingTalkIsvEventService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.starter.amqp.core.RabbitSenderService;
import com.rabbitmq.client.Channel;
import com.vikadata.social.dingtalk.event.sync.http.OrgSuiteChangeEvent;
import jakarta.annotation.Resource;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
public class DingTalkRabbitConsumer {
    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialService iSocialService;

    @Resource
    private IDingTalkIsvEventService iDingTalkIsvEventService;

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @RabbitListener(queues = DING_TALK_ISV_TOPIC_QUEUE_NAME_DEAD)
    public void orgAuthChange(OrgSuiteChangeEvent event, Message message, Channel channel)
        throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("orgAuthChangeQueue received message:{}; deliveryTag:{}", event.getCorpId(),
            deliveryTag);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(event.getCorpId(), event.getSuiteId());
        try {
            if (iSocialService.isContactSyncing(spaceId)) {
                log.info("Space synchronization not completed:{}:{}", event.getCorpId(), spaceId);
                rabbitSenderService.topicSend(DING_TALK_TOPIC_EXCHANGE_BUFFER,
                    DING_TALK_ISV_HIGH_TOPIC, event,
                    Long.toString(120 * 1000));
            } else {
                // Space is synchronizing to avoid duplicate data in the member table
                iSocialService.setContactSyncing(spaceId, event.getCorpId());
                iDingTalkIsvEventService.handleOrgSuiteChangeEvent(event.getSuiteId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", event.getCorpId(), e);
            iSocialService.contactFinished(spaceId);
        }
        // Manual ack, multiple = false, Confirm this message, otherwise confirm all messages before this tag
        // After ack confirmation is enabled in the configuration, when the consumer does not confirm, the message is blocked and will not receive subsequent messages. After disconnection, the unconfirmed message returns to the normal message queue and is consumed repeatedly
        channel.basicAck(deliveryTag, false);
    }
}
