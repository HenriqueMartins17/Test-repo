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
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Third Party Platform Integration - Social Tenant Table
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
@TableName(keepGlobalPrefix = true, value = "social_tenant")
public class SocialTenantEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Application ID
     */
    private String appId;

    /**
     * Application type (1: enterprise internal application, 2: independent service provider)
     */
    private Integer appType;

    /**
     * The unique identifier of the enterprise. The terms of the major platforms are inconsistent. Tenants are used here to represent
     */
    private String tenantId;

    /**
     * Address book permission range
     */
    private String contactAuthScope;

    /**
     * Authorization mode. 1: Enterprise authorization; 2: Member Authorization
     */
    private Integer authMode;

    /**
     * Permanent authorization code
     */
    private String permanentCode;

    /**
     * Enterprise authorization information
     */
    private String authInfo;

    /**
     * Platform (1: WeCom, 2: DingTalk, 3: Feishu)
     */
    private Integer platform;

    /**
     * Status (0: Deactivate, 1: Enable)
     */
    private Boolean status;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
