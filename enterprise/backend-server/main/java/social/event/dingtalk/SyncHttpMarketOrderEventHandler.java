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

package com.apitable.enterprise.social.event.dingtalk;

import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventHandler;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventListener;
import com.apitable.enterprise.social.service.IDingTalkIsvEventService;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Event subscription -- application market order.
 * </p>
 */
@DingTalkEventHandler
@Slf4j
public class SyncHttpMarketOrderEventHandler {

    @Resource
    private IDingTalkIsvEventService iDingTalkIsvEventService;

    /**
     * Application market opens paid applications.
     *
     * @param bizId Order id of the order
     * @param event Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onMarketOrderEvent(String bizId, SyncHttpMarketOrderEvent event) {
        log.info("Received the DingTalk push order event:[{}:{}:{}:{}]", event.getEventType(),
            event.getSyncAction(), event.getCorpId(), bizId);
        String orderId = iDingTalkIsvEventService.handleMarketOrderEvent(event);
        if (StrUtil.isNotBlank(orderId)) {
            SpringContextHolder.getApplicationContext()
                .publishEvent(new SyncOrderEvent(this, orderId));
        }
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Received the DingTalk push service shutdown event.
     *
     * @param bizId Order id of the order
     * @param event Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onMarketServiceCloseEvent(String bizId, SyncHttpMarketServiceCloseEvent event) {
        log.info("Received the Ding Talk push service shutdown event:[{}:{}:{}:{}]",
            event.getEventType(), event.getSyncAction(), event.getCorpId(),
            event.getOrderId());
        iDingTalkIsvEventService.handleMarketServiceClosedEvent(event);
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
