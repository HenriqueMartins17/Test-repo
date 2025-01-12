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
 * Social WeCom Permit Order Table
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
@TableName(keepGlobalPrefix = true, value = "social_wecom_permit_order")
public class SocialWecomPermitOrderEntity implements Serializable {

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
     * Order ID of interface license
     */
    private String orderId;

    /**
     * Order Type.1: Purchase account number; 2: Renewal account number; 5: Historical enterprise migration order
     */
    private Integer orderType;

    /**
     * Order Status.0: To be paid; 1: Paid; 2: Unpaid, the order has been closed; 3: Unpaid, the order has expired; 4: Applying for refund; 5: Refund succeeded; 6: Refund is refused; 7: The order has expired (when the enterprise is removed from the list of service provider test enterprises, all test orders of the corresponding test enterprise will be set as expired)
     */
    private Integer orderStatus;

    /**
     * Order amount, Units: cents
     */
    private Integer price;

    /**
     * Number of basic accounts
     */
    private Integer baseAccountCount;

    /**
     * External Account Count
     */
    private Integer externalAccountCount;

    /**
     * The number of months of purchase is 31 days per month
     */
    private Integer durationMonths;

    /**
     * Create Time of the order
     */
    private LocalDateTime createTime;

    /**
     * Payment time of order
     */
    private LocalDateTime payTime;

    /**
     * WeCom user ID of the subscriber
     */
    private String buyerUserId;

    /**
     * Delete Tag. 0: No, 1: Yes
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
