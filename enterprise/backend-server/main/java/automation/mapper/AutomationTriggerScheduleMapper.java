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

package com.apitable.enterprise.automation.mapper;

import com.apitable.enterprise.automation.entity.AutomationTriggerScheduleEntity;
import com.apitable.enterprise.automation.model.TriggerScheduleDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * automation trigger schedule mapper.
 */
public interface AutomationTriggerScheduleMapper extends BaseMapper<AutomationTriggerScheduleEntity> {
    /**
     * query base info by id.
     *
     * @param id id
     * @return TriggerScheduleDTO
     */
    TriggerScheduleDTO selectScheduleConfAndTriggerStatusById(@Param("id") Long id);

    /**
     * query by trigger ids.
     *
     * @param triggerIds trigger id list
     * @return list of AutomationTriggerScheduleEntity
     */
    List<AutomationTriggerScheduleEntity> selectByTriggerIds(
        @Param("triggerIds") List<String> triggerIds);

    /**
     * query id.
     *
     * @param triggerId trigger id
     * @return AutomationTriggerScheduleEntity
     */
    AutomationTriggerScheduleEntity selectIdByTriggerId(@Param("triggerId") String triggerId);

    /**
     * update is deleted.
     *
     * @param triggerId trigger id
     * @param userId    updater user id
     * @param isDeleted deleted status
     * @return effect rows
     */
    int updateIsDeletedByTriggerId(@Param("triggerId") String triggerId,
                                   @Param("userId") Long userId,
                                   @Param("isDeleted") Boolean isDeleted);
}
