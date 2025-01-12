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
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

/**
 * <p>
 * Third Party Platform Integration - Social Cp Isv Event Log Table
 * </p>
 *
 * @author Mybatis Generator Tool
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
@TableName(keepGlobalPrefix = true, value = "social_cp_isv_event_log")
public class SocialCpIsvEventLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Message notification type.1：create_auth 2：change_auth 3：cancel_auth 11：suite_ticket, third party services suite_ticket；21：change_contact,application change member
     */
    private Integer type;

    /**
     * Suit ID.The appId in the correspond platform
     */
    private String suiteId;

    /**
     * Info Type
     */
    private String infoType;

    /**
     * The authorized enterprise ID. The tenantId in the corresponding platform
     */
    private String authCorpId;

    /**
     * Time Stamp
     */
    private Long timestamp;

    /**
     * Entire message body
     */
    private String message;

    /**
     * Processing status. 1: To be handled; 2: Process failed, please try again; 3: Process failed, end; 4: Process succeeded
     */
    private Integer processStatus;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;

    @Tolerate
    public SocialCpIsvEventLogEntity() {
        // default constructor
    }

}
