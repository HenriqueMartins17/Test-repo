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
 * Social WeCom Permit Order Account Table
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
@TableName(keepGlobalPrefix = true, value = "social_wecom_permit_order_account")
public class SocialWecomPermitOrderAccountEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
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
     * Account type. 1: Basic account number; 2: Interworking account
     */
    private Integer type;

    /**
     * Account status. 1: Unbound; 2: Bound and valid; 3: Has expired; 4: To be transferred
     */
    private Integer activateStatus;

    /**
     * Account activation code
     */
    private String activeCode;

    /**
     * WeCom user ID activated by account binding
     */
    private String cpUserId;

    /**
     * Create Time,Create the order immediately after successful payment
     */
    private LocalDateTime createTime;

    /**
     * Time of first activation of bound user
     */
    private LocalDateTime activeTime;

    /**
     * Expire Time.Add the purchase duration to the first activation binding time
     */
    private LocalDateTime expireTime;

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
