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

package com.apitable.enterprise.automation.service;

import com.apitable.enterprise.automation.entity.AutomationTriggerScheduleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * automation trigger schedule service interface.
 */
public interface IAutomationTriggerScheduleService
    extends IService<AutomationTriggerScheduleEntity> {

    /**
     * publish schedule to mq
     *
     * @param scheduleId schedule id
     */
    void publishTriggerSchedule(Long scheduleId);

    /**
     * publish all schedule.
     */
    void publishAllTriggerScheduleWithoutVerify();

    /**
     * publish the specific schedule.
     *
     * @param scheduleIds list schedule id
     */
    void publishTriggerSchedules(List<Long> scheduleIds);

    /**
     * copy trigger.
     *
     * @param newTriggerMap old triggerId -> new triggerId
     */
    void copy(Map<String, String> newTriggerMap);

    /**
     * create schedule.
     *
     * @param spaceId        space id
     * @param triggerId      trigger id
     * @param scheduleConfig config
     */
    void createTriggerSchedule(String spaceId, String triggerId, String scheduleConfig);

    /**
     * update schedule.
     *
     * @param triggerId      trigger id
     * @param scheduleConfig config
     */
    void updateScheduleConfig(String triggerId, String scheduleConfig);

    /**
     * delete schedule.
     *
     * @param triggerId trigger id
     */
    void deleteByTriggerId(String triggerId, Long userId);
}
