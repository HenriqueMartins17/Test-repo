/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.social.service.dingtalk.event;

import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventHandler;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventListener;

import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * Event Subscriptions -- Apply Marketplace Orders
 */
@DingTalkEventHandler
@Slf4j
public class MarketOrderEventHandler {

    /**
     * application market opens paid applications
     *
     * @param bizId The orderid of the order.
     * @param event event content
     * @return response content
     */
    @DingTalkEventListener
    public Object onMarketOrderEvent(String bizId, SyncHttpMarketOrderEvent event) {
        log.info("DingTalk push order event received: [{}:{}:{}:{}], not processed", event.getEventType(),
                event.getSyncAction(), event.getCorpId(),
                bizId);
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * App Market Service Shutdown Notification.
     * The user action corresponding to the order.
     * When sync Action is market service close, it means that the service is closed due to order expiration or user refund.
     * Note: Only service shutdowns due to refunds are currently being pushed.
     *
     * @param bizId orderid of the order
     * @param event event content
     * @return response content
     */
    @DingTalkEventListener
    public Object onMarketServiceCloseEvent(String bizId, SyncHttpMarketServiceCloseEvent event) {
        log.info("Received DingTalk push service shutdown event: [{}:{}:{}:{}], not processed", event.getEventType(),
                event.getSyncAction(), event.getCorpId(),
                bizId);
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
