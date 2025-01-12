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

package com.apitable.enterprise.vikabilling.service;

import com.apitable.enterprise.vikabilling.entity.OrderPaymentEntity;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * Order Payment Service
 * </p>
 */
public interface IOrderPaymentService extends IService<OrderPaymentEntity> {

    /**
     * Get order payment by pay transaction id
     *
     * @param payTransactionId pay transaction id
     * @return FinanceOrderPaymentEntity
     */
    OrderPaymentEntity getByPayTransactionId(String payTransactionId);

    /**
     * Get order payment list by order id
     *
     * @param orderId order id
     * @return order payment list
     */
    List<OrderPaymentEntity> getByOrderId(String orderId);

    /**
     * Payment success callback notification event processing
     *
     * @param chargeSuccess payment success event notification
     * @return orderId
     */
    String retrieveOrderPaidEvent(ChargeSuccess chargeSuccess);
}
