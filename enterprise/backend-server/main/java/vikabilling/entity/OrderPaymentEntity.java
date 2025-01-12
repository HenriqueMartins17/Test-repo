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
 * Billing System - Billing Order Payment Table
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
@TableName(keepGlobalPrefix = true, value = "billing_order_payment")
public class OrderPaymentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Order ID
     */
    private String orderId;

    /**
     * Payment transaction No
     */
    private String paymentTransactionId;

    /**
     * ISO currency code (upper case letters)
     */
    private String currency;

    /**
     * Amount (the minimum monetary unit of the corresponding currency, RMB in cents)
     */
    private Integer amount;

    /**
     * Third party payment item title
     */
    private String subject;

    /**
     * Attribute value of payment channel
     */
    private String payChannel;

    /**
     * Payment channel transaction ID
     */
    private String payChannelTransactionId;

    /**
     * Payment time
     */
    private LocalDateTime paidTime;

    /**
     * Payment succeeded (0: No, 1: Yes)
     */
    private Boolean paymentSuccess;

    /**
     * Payment callback notification source data
     */
    private String rawData;

    /**
     * Description
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
