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

package com.apitable.enterprise.automation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Automation - trigger schedule entity.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
@TableName(keepGlobalPrefix = true, value = "automation_trigger_schedule")
public class AutomationTriggerScheduleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private BigInteger id;

    /**
     * space ID (link#xxxx_space#space_id).
     */
    private String spaceId;

    /**
     * trigger ID (link#xxxx_automation_trigger#trigger_id).
     */
    private String triggerId;

    /**
     * Trigger schedule config.
     */
    private String scheduleConf;

    /**
     * Scheduling status: 0-stop, 1-pushed, 2-running.
     */
    private Integer triggerStatus;

    /**
     * 0: No, 1: Yes
     */
    private Boolean isPushed;

    /**
     * Last scheduled time.
     */
    private LocalDateTime triggerLastTime;

    /**
     * Next scheduling time.
     */
    private LocalDateTime triggerNextTime;

    /**
     * Delete Tag(0: No, 1: Yes)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * Creator
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * Last Update By
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;
}
