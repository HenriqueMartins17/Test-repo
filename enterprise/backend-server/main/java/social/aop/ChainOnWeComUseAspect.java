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

package com.apitable.enterprise.social.aop;

import java.time.Duration;
import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.mapper.SocialTenantMapper;
import com.apitable.enterprise.social.service.IWeComService;
import com.apitable.shared.aspect.BaseAspectSupport;
import com.apitable.core.exception.BusinessException;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.model.WeComAuthInfo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <p>
 * wecomTemplate app context AOP
 * </p>
 *
 * @author Pengap
 */
@Aspect
@Component
@ConditionalOnProperty(value = "social.wecom.enabled", havingValue = "true")
@Slf4j
public class ChainOnWeComUseAspect extends BaseAspectSupport {

    @Resource
    private SocialTenantMapper socialTenantMapper;

    @Resource
    private IWeComService iWeComService;

    @Resource
    private WeComTemplate weComTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    private static final int TIMEOUT = 2;

    @Before("execution(* com.vikadata.social.wecom.WeComTemplate.switchoverTo(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (ArrayUtil.isEmpty(args) || args.length < 1) {
            throw new BusinessException("can't switch because appId lost");
        }
        String corpId = ArrayUtil.get(args, 0);
        Integer agentId = ArrayUtil.get(args, 1);
        boolean isTempAuthService = ObjectUtil.defaultIfNull(ArrayUtil.get(args, 2), false);

        String key = weComTemplate.mergeKey(corpId, agentId);
        boolean isExistService = weComTemplate.isExistService(key, isTempAuthService);
        if (isTempAuthService && !isExistService) {
            Duration timeout =
                Duration.ofHours(TIMEOUT).plusSeconds(RandomUtil.randomLong(60, 360));

            String configSha = SecureUtil.sha1(String.format("%s-%s", corpId, agentId));
            WeComAuthInfo authConfig = iWeComService.getConfigSha(configSha);
            if (null == authConfig) {
                throw new BusinessException("get wecom config error");
            }
            weComTemplate.addService(corpId, agentId, authConfig.getAgentSecret(), true,
                timeout.toMillis());
        } else if (!isTempAuthService && !isExistService) {
            WeComAuthInfo authConfig = this.cacheGetConfig(corpId, agentId);
            weComTemplate.addService(corpId, agentId, authConfig.getAgentSecret());
        }
    }

    /**
     * get auth config , If there is a cache, get the cache content first
     *
     * @param corpId  corp id
     * @param agentId corp app id
     * @return wecom auth info object
     */
    private WeComAuthInfo cacheGetConfig(String corpId, Integer agentId) {
        String key = weComTemplate.getCacheConfigKeyPrefix()
            .concat(StrUtil.format("{}:{}", corpId, String.valueOf(agentId)));
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        WeComAuthInfo authConfig = (WeComAuthInfo) ops.get();
        SocialTenantEntity config;
        try {
            if (Objects.nonNull(authConfig)) {
                return authConfig;
            }
            config = socialTenantMapper.selectByAppIdAndTenantId(String.valueOf(agentId), corpId);
            if (null == config || StrUtil.isBlank(config.getAuthInfo())) {
                throw new BusinessException("can't get corp auth info");
            } else if (BooleanUtil.isFalse(config.getStatus())) {
                throw new BusinessException(String.format("this corp %s is disable", corpId));
            } else {
                // To prevent a large number of keys from failing in batches in the same time period, add random numbers
                Duration timeout = Duration.ofHours(11).plusSeconds(RandomUtil.randomLong(60, 360));
                authConfig = objectMapper.readValue(config.getAuthInfo(), WeComAuthInfo.class);
                ops.set(authConfig, timeout);
                return authConfig;
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("parse corp auth info error");
        }
    }

}
