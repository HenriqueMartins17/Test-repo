/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.model;

import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.Product;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * <p>
 * the third party orders.
 * </p>
 */
@Data
@Builder(toBuilder = true)
public class SocialOrderContext {

    private ProductChannel productChannel;

    private String socialOrderId;

    /**
     * payment Amount Unit: cents.
     */
    private Long amount;

    /**
     * discount amount Unit: cents.
     */
    @Default
    private Long discountAmount = 0L;

    /**
     * Total price of the original order Unit: cents.
     */
    private Long originalAmount;

    private Price price;

    private Product product;

    private String spaceId;

    private LocalDateTime paidTime;

    private LocalDateTime createdTime;

    private LocalDateTime serviceStartTime;

    private LocalDateTime serviceStopTime;

    /**
     * subscription phase.
     */
    @Default
    private SubscriptionPhase phase = SubscriptionPhase.FIXEDTERM;

    private Bundle activatedBundle;

    private OrderChannel orderChannel;

    private OrderType orderType;
}
