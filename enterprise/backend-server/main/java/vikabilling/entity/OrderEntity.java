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

package com.apitable.enterprise.vikabilling.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Billing System - Billing Order Table
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
@TableName(keepGlobalPrefix = true, value = "billing_order")
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Space ID
     */
    private String spaceId;

    /**
     * Order ID
     */
    private String orderId;

    /**
     * Order Channel(vika,lark,dingtalk,wecom)
     */
    private String orderChannel;

    /**
     * Order ID of other channels
     */
    private String channelOrderId;

    /**
     * Order Type(BUY,UPGRADE,RENEW)
     */
    private String orderType;

    /**
     * ISO currency code (upper case letters)
     */
    private String currency;

    /**
     * Total order amount  (minimum currency unit of corresponding currency , RMB is cents)
     */
    private Integer originalAmount;

    /**
     * Total discount amount  (minimum currency unit of corresponding currency , RMB is cents)
     */
    private Integer discountAmount;

    /**
     * Total pay amount  (minimum currency unit of corresponding currency , RMB is cents)
     */
    private Integer amount;

    /**
     * Order Status(created,paid,refunded,canceled)
     */
    private String state;

    /**
     * Order Create Time
     */
    private LocalDateTime createdTime;

    /**
     * Paid or not (0: No, 1: Yes)
     */
    private Boolean isPaid;

    /**
     * Order payment completion time
     */
    private LocalDateTime paidTime;

    /**
     * Remark
     */
    private String remark;

    /**
     * Delete Tag(0: No, 1: Yes)
     */
    @TableLogic
    private Boolean isDeleted;

    /**
     * Optimistic lock version number
     */
    @Version
    private Integer version;

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
