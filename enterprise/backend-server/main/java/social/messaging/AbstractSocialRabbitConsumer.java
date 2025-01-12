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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;

import static com.apitable.enterprise.social.redis.RedisKey.getSocialIsvEventLockKey;
import static com.apitable.enterprise.social.redis.RedisKey.getSocialIsvEventProcessingKey;

/**
 * <p>
 * social consumer abstract class
 * </p>
 */
@Slf4j
public class AbstractSocialRabbitConsumer {

    /**
     * 5 seconds wait for lock
     */
    protected static final long WAIT_LOCK_MILLIS = 5000L;

    protected static final Integer MAX_EVENT_HANDLE_SECONDS = 3600;

    @Resource
    private RedisLockRegistry redisLockRegistry;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    protected Lock getTenantEventLock(String tenantId, String appId) {
        return redisLockRegistry.obtain(getSocialIsvEventLockKey(tenantId, appId));
    }

    /**
     * flag working on an enterprise event
     *
     * @param tenantId corp id
     * @param appId    app id
     */
    protected Boolean setTenantEventOnProcessing(String tenantId, String appId, String eventId) {
        return redisTemplate.opsForValue()
            .setIfAbsent(getSocialIsvEventProcessingKey(tenantId, appId),
                eventId, MAX_EVENT_HANDLE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * flag working on an enterprise event
     *
     * @param tenantId corp id
     * @param appId    app id
     */
    protected Boolean tenantEventOnProcessing(String tenantId, String appId) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(getSocialIsvEventProcessingKey(tenantId, appId)));
    }

    protected void setTenantEventOnProcessed(String tenantId, String appId) {
        String key = getSocialIsvEventProcessingKey(tenantId, appId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.delete(key);
        }
    }

    protected String getTenantEventOnProcessingId(String tenantId, String appId) {
        String key = getSocialIsvEventProcessingKey(tenantId, appId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }
}
