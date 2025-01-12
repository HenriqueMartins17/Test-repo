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

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventHandler;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventListener;
import com.apitable.core.util.SpringContextHolder;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.config.DingTalkRedisOperations;
import com.vikadata.social.dingtalk.enums.DingTalkBizType;
import com.vikadata.social.dingtalk.event.BaseSyncHttpBizData;
import com.vikadata.social.dingtalk.event.SyncHttpPushHighEvent;
import com.vikadata.social.dingtalk.event.SyncHttpPushMediumEvent;

import org.springframework.beans.factory.annotation.Autowired;

import static com.apitable.enterprise.social.redis.RedisKey.getDingTalkSyncHttpEventLockKey;
import static com.vikadata.social.dingtalk.DingTalkServiceProvider.EVENT_CALLBACK_TYPE_KEY;
import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * <p>
 * Event subscription - high priority data, application activation, etc
 * Event subscription -- common priority data, such as address book change
 * </p>
 */
@DingTalkEventHandler
@Slf4j
public class SyncHttpIsvEventHandler {
    @Autowired(required = false)
    private DingTalkRedisOperations dingTalkRedisOperations;

    /**
     * Processing high priority data, activating applications, etc
     *
     * @param event Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onSyncHttpPushHighEvent(String suiteId, SyncHttpPushHighEvent event) {
        log.info("ISV DingTalk push event received[{}]", event.getEventType());
        DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
        // Sort by biz type first, from small to large, because priority is required
        List<BaseSyncHttpBizData> bizDataList = event.getBizData().stream().sorted(Comparator.comparing(BaseSyncHttpBizData::getBizType)).collect(Collectors.toList());
        for (BaseSyncHttpBizData bizData : bizDataList) {
            if (!DingTalkBizType.hasValue(bizData.getBizType())) {
                log.error("Received an undetermined biz type processing event:{}", bizData.getBizType());
                continue;
            }
            String lockKey = getDingTalkSyncHttpEventLockKey(bizData.getSubscribeId(),
                    bizData.getCorpId(), bizData.getBizId(), bizData.getBizType());
            // 60s
            Lock lock = dingTalkRedisOperations.getLock(lockKey);
            boolean locked = false;
            try {
                // Even if the data is received, it can only be processed by one client at a time
                locked = lock.tryLock();
                if (locked) {
                    JSONObject eventData = JSONUtil.parseObj(bizData.getBizData());
                    eventData.set(EVENT_CALLBACK_TYPE_KEY, event.getEventType());
                    dingtalkServiceProvider.handleIsvAppEventNotify(bizData.getBizId(), eventData, bizData.getCorpId(), suiteId);
                }
            }
            catch (Exception e) {
                log.error("Handling Sync Action exceptions:{}:{}", bizData.getBizId(), bizData.getBizType(), e);
            }
            finally {
                if (locked) {
                    lock.unlock();
                }
            }
        }
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Common priority data, such as address book change
     *
     * @param event Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onSyncHttpPushMediumEvent(String suiteId, SyncHttpPushMediumEvent event) {
        log.info("Received DingTalk push event{}", event.getEventType());
        DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
        for (BaseSyncHttpBizData bizData : event.getBizData()) {
            if (!DingTalkBizType.hasValue(bizData.getBizType())) {
                log.error("Received an undetermined biz type processing event:{}", bizData.getBizType());
                return null;
            }
            String lockKey = getDingTalkSyncHttpEventLockKey(bizData.getSubscribeId(),
                    bizData.getCorpId(), bizData.getBizId(), bizData.getBizType());
            // 60s
            Lock lock = dingTalkRedisOperations.getLock(lockKey);
            boolean locked = false;
            try {
                // Even if the data is received, it can only be processed by one client at a time
                locked = lock.tryLock();
                if (locked) {
                    JSONObject eventData = JSONUtil.parseObj(bizData.getBizData());
                    eventData.set(EVENT_CALLBACK_TYPE_KEY, event.getEventType());
                    dingtalkServiceProvider.handleIsvAppEventNotify(bizData.getBizId(), eventData, bizData.getCorpId(), suiteId);
                }
            }
            catch (Exception e) {
                log.error("Handling Sync Action exceptions:{}:{}", bizData.getBizId(), bizData.getBizType(), e);
            }
            finally {
                if (locked) {
                    lock.unlock();
                }
            }
        }
        // The event push of Ding Talk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
