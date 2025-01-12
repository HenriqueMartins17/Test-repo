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

package com.apitable.enterprise.vikabilling.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import cn.hutool.core.util.BooleanUtil;
import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class OrderChecker {

    private final IOrderV2Service iOrderV2Service;

    public OrderChecker(IOrderV2Service iOrderV2Service) {
        this.iOrderV2Service = iOrderV2Service;
    }

    public void check(String orderId, ExpectedOrderCheck check) {
        OrderEntity orderEntity = iOrderV2Service.getByOrderId(orderId);
        assertThat(orderEntity).isNotNull();
        AtomicReference<String> failMessage = new AtomicReference<>("");
        boolean checkOrderData = ((Predicate<OrderEntity>) order -> {
            if (check.getOrderType() != null &&
                check.getOrderType() != OrderType.of(order.getOrderType())) {
                failMessage.set(
                    String.format("order type expect %s, but actually %s", check.getOrderType(),
                        order.getOrderType()));
                return false;
            }
            if (check.getOriginalAmount() != order.getOriginalAmount()) {
                failMessage.set(
                    String.format("order origin amount expect %s, but actually %s",
                        check.getOriginalAmount(),
                        order.getOriginalAmount()));
                return false;
            }

            if (check.getDiscountAmount() != order.getDiscountAmount()) {
                failMessage.set(
                    String.format("order discount amount expect %s, but actually %s",
                        check.getDiscountAmount(),
                        order.getDiscountAmount()));
                return false;
            }

            if (check.getAmount() != order.getAmount()) {
                failMessage.set(
                    String.format("order paid amount expect %s, but actually %s", check.getAmount(),
                        order.getAmount()));
                return false;
            }

            if (check.getOrderStatus() != OrderStatus.of(order.getState())) {
                failMessage.set(
                    String.format("order status expect %s, but actually %s",
                        check.getOrderStatus().getName(),
                        order.getState()));
                return false;
            }

            if (check.hasPaid()) {
                if (BooleanUtil.isFalse(order.getIsPaid())) {
                    failMessage.set(
                        String.format("order paid status expect %s, but actually %s",
                            check.hasPaid(),
                            order.getIsPaid())
                    );
                    return false;
                }
                boolean paidTimeCheck = order.getPaidTime() != null
                    && check.getPaidTime().isEqual(order.getPaidTime());
                if (!paidTimeCheck) {
                    failMessage.set(
                        String.format("order paid time expect %s, but actually %s",
                            check.getPaidTime(),
                            order.getPaidTime())
                    );
                    return false;
                }
                return true;
            }
            return true;
        }).test(orderEntity);
        if (!checkOrderData) {
            fail("order check fail, reason: " + failMessage.get());
        }
    }

    public static class ExpectedOrderCheck {

        private final OrderType orderType;

        private final int originalAmount;

        private final int discountAmount;

        private final int amount;

        private final OrderStatus orderStatus;

        private final boolean isPaid;

        private final LocalDateTime paidTime;

        public ExpectedOrderCheck(OrderType orderType, int originalAmount, int discountAmount,
                                  int amount, OrderStatus orderStatus, boolean isPaid,
                                  LocalDateTime paidTime) {
            this.orderType = orderType;
            this.originalAmount = originalAmount;
            this.discountAmount = discountAmount;
            this.amount = amount;
            this.orderStatus = orderStatus;
            this.isPaid = isPaid;
            this.paidTime = paidTime;
        }

        public OrderType getOrderType() {
            return orderType;
        }

        public int getOriginalAmount() {
            return originalAmount;
        }

        public int getDiscountAmount() {
            return discountAmount;
        }

        public int getAmount() {
            return amount;
        }

        public OrderStatus getOrderStatus() {
            return orderStatus;
        }

        public boolean hasPaid() {
            return isPaid;
        }

        public LocalDateTime getPaidTime() {
            return paidTime;
        }
    }
}
