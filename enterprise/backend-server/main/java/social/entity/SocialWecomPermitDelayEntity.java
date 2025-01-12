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

package com.apitable.enterprise.social.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Social WeCom Permit Delay Table
 * </p>
 *
 * @author Mybatis Generator Tool
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
@TableName(keepGlobalPrefix = true, value = "social_wecom_permit_delay")
public class SocialWecomPermitDelayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Suite ID
     */
    private String suiteId;

    /**
     * Authorized enterprise ID
     */
    private String authCorpId;

    /**
     * First authorized installation time
     */
    private LocalDateTime firstAuthTime;

    /**
     * Delay processing type. 1: Notice of expiration of free trial of interface license; 2: Enterprise pays for delayed purchase of interface license
     */
    private Integer delayType;

    /**
     * Processing status. 0: To be processed; 1: Has been sent to the queue; 5: Orders have been placed; 9: Completed
     */
    private Integer processStatus;

    /**
     * Delete Tag. 0: No, 1: Yes
     */
    @TableLogic
    private Boolean isDeleted;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
