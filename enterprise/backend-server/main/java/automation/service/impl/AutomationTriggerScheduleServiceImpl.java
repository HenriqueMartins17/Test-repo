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

package com.apitable.enterprise.automation.service.impl;

import static com.apitable.enterprise.automation.autoconfigure.AutomationQueueConfig.AUTOMATION_EVENT_CREATE_ROUTING_KEY;
import static com.apitable.enterprise.automation.autoconfigure.AutomationQueueConfig.AUTOMATION_EVENT_EXCHANGE;

import cn.hutool.json.JSONUtil;
import com.apitable.core.constants.RedisConstants;
import com.apitable.enterprise.automation.entity.AutomationTriggerScheduleEntity;
import com.apitable.enterprise.automation.enums.TriggerScheduleStatus;
import com.apitable.enterprise.automation.mapper.AutomationTriggerScheduleMapper;
import com.apitable.enterprise.automation.model.TriggerScheduleDTO;
import com.apitable.enterprise.automation.model.TriggerScheduleMessageDTO;
import com.apitable.enterprise.automation.service.IAutomationTriggerScheduleService;
import com.apitable.starter.amqp.core.RabbitSenderService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * automation trigger schedule service implement.
 */
@Slf4j
@Service
public class AutomationTriggerScheduleServiceImpl
    extends ServiceImpl<AutomationTriggerScheduleMapper, AutomationTriggerScheduleEntity>
    implements IAutomationTriggerScheduleService {

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @Resource
    private AutomationTriggerScheduleMapper automationTriggerScheduleMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void publishTriggerSchedule(Long scheduleId) {
        TriggerScheduleDTO triggerSchedule =
            automationTriggerScheduleMapper.selectScheduleConfAndTriggerStatusById(scheduleId);
        if (null == triggerSchedule || triggerSchedule.getIsPushed() ||
            triggerSchedule.getScheduleConf().equals(
                JSONUtil.toJsonStr(JSONUtil.toJsonStr(JSONUtil.createObj())))) {
            return;
        }
        if (null != triggerSchedule.getScheduleConf()) {
            rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                TriggerScheduleMessageDTO.builder().scheduleId(scheduleId.toString()).build());
            AutomationTriggerScheduleEntity entity = AutomationTriggerScheduleEntity.builder()
                .id(BigInteger.valueOf(scheduleId))
                .triggerStatus(TriggerScheduleStatus.PENDING.getStatus())
                .isPushed(true)
                .build();
            automationTriggerScheduleMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAllTriggerScheduleWithoutVerify() {
        Page<AutomationTriggerScheduleEntity> result;
        long current = 1L;
        do {
            result = page(Page.of(current, 100));
            List<AutomationTriggerScheduleEntity> entities = result.getRecords().stream()
                .filter(i -> !i.getScheduleConf()
                    .equals(JSONUtil.toJsonStr(JSONUtil.toJsonStr(JSONUtil.createObj()))))
                .map(i -> {
                    rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                        AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                        TriggerScheduleMessageDTO.builder().scheduleId(i.getId().toString())
                            .build());
                    return AutomationTriggerScheduleEntity.builder()
                        .id(i.getId())
                        .triggerStatus(TriggerScheduleStatus.PENDING.getStatus())
                        .isPushed(true)
                        .build();
                }).toList();
            updateBatchById(entities, entities.size());
            current = current + 1L;
        } while (result.hasNext());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishTriggerSchedules(List<Long> scheduleIds) {
        List<AutomationTriggerScheduleEntity> entities =
            automationTriggerScheduleMapper.selectBatchIds(scheduleIds);
        List<AutomationTriggerScheduleEntity> updateEntities = new ArrayList<>();
        for (AutomationTriggerScheduleEntity entity : entities) {
            rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                TriggerScheduleMessageDTO.builder().scheduleId(entity.getId().toString()).build());
            updateEntities.add(AutomationTriggerScheduleEntity.builder()
                .id(entity.getId())
                .triggerStatus(TriggerScheduleStatus.PENDING.getStatus())
                .isPushed(true)
                .build());
        }
        updateBatchById(updateEntities, entities.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copy(Map<String, String> newTriggerMap) {
        List<String> oldTriggerIds = newTriggerMap.keySet().stream().toList();
        List<AutomationTriggerScheduleEntity> entities =
            automationTriggerScheduleMapper.selectByTriggerIds(oldTriggerIds);
        if (entities.isEmpty()) {
            return;
        }
        List<AutomationTriggerScheduleEntity> createEntities = new ArrayList<>();
        for (AutomationTriggerScheduleEntity entity : entities) {
            AutomationTriggerScheduleEntity createEntity = AutomationTriggerScheduleEntity.builder()
                .id(BigInteger.valueOf(IdWorker.getId()))
                .triggerId(newTriggerMap.get(entity.getTriggerId()))
                .spaceId(entity.getSpaceId())
                .scheduleConf(entity.getScheduleConf())
                .triggerStatus(TriggerScheduleStatus.PENDING.getStatus())
                .isPushed(true)
                .build();
            rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                TriggerScheduleMessageDTO.builder().scheduleId(createEntity.getId().toString())
                    .build());
            createEntities.add(createEntity);
        }
        saveBatch(createEntities);
    }

    @Override
    public void createTriggerSchedule(String spaceId, String triggerId, String scheduleConfig) {
        AutomationTriggerScheduleEntity createEntity = AutomationTriggerScheduleEntity.builder()
            .id(BigInteger.valueOf(IdWorker.getId()))
            .triggerId(triggerId)
            .spaceId(spaceId)
            .scheduleConf(scheduleConfig)
            .triggerStatus(TriggerScheduleStatus.PENDING.getStatus())
            .build();
        if (!scheduleConfig.equals(JSONUtil.toJsonStr(JSONUtil.createObj()))) {
            rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                TriggerScheduleMessageDTO.builder().scheduleId(createEntity.getId().toString())
                    .build());
            createEntity.setIsPushed(true);
        }
        save(createEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScheduleConfig(String triggerId, String scheduleConfig) {
        AutomationTriggerScheduleEntity entity =
            automationTriggerScheduleMapper.selectIdByTriggerId(triggerId);
        if (null == entity) {
            return;
        }
        entity.setScheduleConf(scheduleConfig);
        // add lock for mq push
        if (!scheduleConfig.equals(JSONUtil.toJsonStr(JSONUtil.createObj())) &&
            !entity.getIsPushed()) {
            Boolean unLock = redisTemplate.opsForValue()
                .setIfAbsent(RedisConstants.triggerUpdateLockKey(triggerId), triggerId, 120 * 1000,
                    TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(unLock)) {
                rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
                    AUTOMATION_EVENT_CREATE_ROUTING_KEY,
                    TriggerScheduleMessageDTO.builder().scheduleId(entity.getId().toString())
                        .build());
            }
            entity.setIsPushed(true);
        } else {
            entity.setIsPushed(false);
        }
        updateById(entity);
    }

    @Override
    public void deleteByTriggerId(String triggerId, Long userId) {
        baseMapper.updateIsDeletedByTriggerId(triggerId, userId, true);
    }

}
