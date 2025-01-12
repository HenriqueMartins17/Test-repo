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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.service.dingtalk.util.SpringContextHolder;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.enums.DingTalkBizType;
import com.vikadata.social.dingtalk.event.BaseSyncHttpBizData;
import com.vikadata.social.dingtalk.event.SyncHttpPushHighEvent;
import com.vikadata.social.dingtalk.event.SyncHttpPushMediumEvent;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventHandler;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventListener;
import com.vikadata.social.service.dingtalk.component.TaskManager;
import com.vikadata.social.service.dingtalk.service.IDingTalkOpenSyncBizDataMediumService;
import com.vikadata.social.service.dingtalk.service.IDingTalkOpenSyncBizDataService;

import static com.vikadata.social.dingtalk.DingTalkServiceProvider.EVENT_CALLBACK_TYPE_KEY;
import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * Event subscriptions -- high-priority data, activation of apps, etc.
 * Event Subscriptions -- normal priority data, such as address book changes
 */
@DingTalkEventHandler
@Slf4j
public class SyncHttpEventHandler {
    @Resource
    private IDingTalkOpenSyncBizDataService iDingTalkOpenSyncBizDataService;

    @Resource
    private IDingTalkOpenSyncBizDataMediumService iDingTalkOpenSyncBizDataMediumService;

    /**
     * Process high-priority data, activate apps, etc.
     *
     * @param event event content
     * @return response content
     */
    @DingTalkEventListener
    public Object onSyncHttpPushHighEvent(String suiteId, SyncHttpPushHighEvent event) {
        log.info("Receive ISV Dingding push event[{}]", event.getEventType());
        DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
        // First sort according to biz Type, from small to large
        List<BaseSyncHttpBizData> bizDataList = event.getBizData().stream().sorted(Comparator.comparing(BaseSyncHttpBizData::getBizType)).collect(Collectors.toList());
        for (BaseSyncHttpBizData bizData : bizDataList) {
            // Asynchronous creation to prevent performance impact
            TaskManager.me().execute(() -> iDingTalkOpenSyncBizDataService.create(bizData.getSubscribeId(),
                    bizData.getCorpId(), bizData.getBizType(), bizData.getBizId(), bizData.getBizData()));
            if (!DingTalkBizType.hasValue(bizData.getBizType())) {
                log.error("Received undefined bizType event:{}", bizData.getBizType());
                continue;
            }
            JSONObject eventData = JSONUtil.parseObj(bizData.getBizData());
            eventData.set(EVENT_CALLBACK_TYPE_KEY, event.getEventType());
            try {
                dingtalkServiceProvider.handleIsvAppEventNotify(suiteId, eventData, bizData.getCorpId(), suiteId);
            }
            catch (Exception e) {
                log.error("Handling syncAction exceptions:{}:{}", bizData.getBizId(), bizData.getBizType(), e);
            }
        }
        // DingTalk's event push will not be repeated. It will return true anyway to ensure service stability,
        // and business exceptions will be handled by lower-level services.
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Normal priority data, such as address book changes
     *
     * @param event event content
     * @return response content
     */
    @DingTalkEventListener
    public Object onSyncHttpPushMediumEvent(String suiteId, SyncHttpPushMediumEvent event) {
        log.info("Receive DingTalk push event{}", event.getEventType());
        DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
        for (BaseSyncHttpBizData bizData : event.getBizData()) {
            // Asynchronous creation to prevent performance impact,
            // high priority and medium priority are stored separately with DingTalk
            TaskManager.me().execute(() -> iDingTalkOpenSyncBizDataMediumService.create(bizData.getSubscribeId(),
                    bizData.getCorpId(), bizData.getBizType(), bizData.getBizId(), bizData.getBizData()));
            if (!DingTalkBizType.hasValue(bizData.getBizType())) {
                log.error("Received undefined bizType event:{}", bizData.getBizType());
                return null;
            }
            JSONObject eventData = JSONUtil.parseObj(bizData.getBizData());
            eventData.set(EVENT_CALLBACK_TYPE_KEY, event.getEventType());
            try {
                dingtalkServiceProvider.handleIsvAppEventNotify(bizData.getBizId(), eventData, bizData.getCorpId(), suiteId);
            }
            catch (Exception e) {
                log.error("Handling syncAction exceptions:{}:{}", bizData.getBizId(), bizData.getBizType(), e);
            }
        }
        // DingTalk's event push will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
