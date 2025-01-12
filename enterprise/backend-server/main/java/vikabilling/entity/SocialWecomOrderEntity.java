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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Billing System - Billing Social WeCom Order
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
@TableName(keepGlobalPrefix = true, value = "billing_social_wecom_order")
public class SocialWecomOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * WeCom Order ID
     */
    private String orderId;

    /**
     * Order Status.0: To be paid; 1: Paid; 2: Cancelled; 3: Payment is overdue; 4: Applying for refund; 5: Refund succeeded; 6: Refund refused
     */
    private Integer orderStatus;

    /**
     * Order Type.0: New apps; 1: Number of users for capacity expansion; 2: Renewal application time; 3: Changed version
     */
    private Integer orderType;

    /**
     * Enterprise ID of the order
     */
    private String paidCorpId;

    /**
     * The order placing operator userid. If the order is placed by the service provider, this field is unavailable
     */
    private String operatorId;

    /**
     * Suite ID
     */
    private String suiteId;

    /**
     * Purchased app version ID
     */
    private String editionId;

    /**
     * Price payable. Unit: cents
     */
    private Integer price;

    /**
     * Number of purchasers
     */
    private Long userCount;

    /**
     * Duration of purchase. Unit: day
     */
    private Integer orderPeriod;

    /**
     * Order time 
     */
    private LocalDateTime orderTime;

    /**
     * Payment time
     */
    private LocalDateTime paidTime;

    /**
     * Start Time of the purchase effective period
     */
    private LocalDateTime beginTime;

    /**
     * End Time of Purchase Effective Period
     */
    private LocalDateTime endTime;

    /**
     * Place the order from. 0: The enterprise places an order; 1: The service provider places orders on behalf; 2: Agent orders
     */
    private Integer orderFrom;

    /**
     * Enterprise ID of the order
     */
    private String operatorCorpId;

    /**
     * The amount shared by the service provider. Unit: cents
     */
    private Integer serviceShareAmount;

    /**
     * Platform share amount. Unit: cents
     */
    private Integer platformShareAmount;

    /**
     * Agent share amount. Unit: cents
     */
    private Integer dealerShareAmount;

    /**
     * Agent enterprise ID
     */
    private String dealerCorpId;

    /**
     * Order original data
     */
    private String orderInfo;

    /**
     * Delete Tag(0: No, 1: Yes)
     */
    @TableLogic
    private Boolean isDeleted;

    /**
     * Creator
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Last Update By
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
