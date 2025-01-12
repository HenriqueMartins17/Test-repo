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

package com.apitable.enterprise.vikabilling.listener;

import static com.apitable.enterprise.vikabilling.util.OrderUtil.centsToYuan;

import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;
import com.apitable.enterprise.vikabilling.entity.OrderPaymentEntity;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderPaymentService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vika.core.VikaOperations;
import com.apitable.enterprise.vika.core.model.BillingOrder;
import com.apitable.enterprise.vika.core.model.BillingOrderItem;
import com.apitable.enterprise.vika.core.model.BillingOrderPayment;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SyncOrderListener implements ApplicationListener<SyncOrderEvent> {

    @Resource
    private IOrderV2Service iOrderV2Service;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IOrderPaymentService iOrderPaymentService;

    @Autowired(required = false)
    private VikaOperations vikaOperations;

    @Async
    @Override
    public void onApplicationEvent(SyncOrderEvent event) {
        if (vikaOperations == null) {
            return;
        }
        OrderEntity orderEntity = iOrderV2Service.getByOrderId(event.getOrderId());
        BillingOrder order = ofOrderEntity(orderEntity);
        List<OrderItemEntity> orderItemEntities =
            iOrderItemService.getByOrderId(event.getOrderId());
        List<BillingOrderItem> orderItems = ofItemEntities(orderItemEntities);
        List<OrderPaymentEntity> orderPaymentEntities =
            iOrderPaymentService.getByOrderId(event.getOrderId());
        List<BillingOrderPayment> orderPayments = ofPaymentEntities(orderPaymentEntities);
        vikaOperations.syncOrder(order, orderItems, orderPayments);
    }

    private BillingOrder ofOrderEntity(OrderEntity entity) {
        BillingOrder order = new BillingOrder();
        order.setOrderId(entity.getOrderId());
        order.setSpaceId(entity.getSpaceId());
        order.setOrderChannel(entity.getOrderChannel());
        order.setChannelOrderId(entity.getChannelOrderId());
        order.setOrderType(entity.getOrderType());
        order.setOriginalAmount(centsToYuan(entity.getOriginalAmount()));
        order.setDiscountAmount(centsToYuan(entity.getDiscountAmount()));
        order.setAmount(centsToYuan(entity.getAmount()));
        order.setCreatedTime(entity.getCreatedTime());
        order.setPaid(entity.getIsPaid());
        order.setPaidTime(entity.getPaidTime());
        return order;
    }

    private List<BillingOrderItem> ofItemEntities(List<OrderItemEntity> entities) {
        List<BillingOrderItem> items = new ArrayList<>();
        entities.forEach(entity -> {
            BillingOrderItem item = new BillingOrderItem();
            item.setOrderId(entity.getOrderId());
            item.setProductName(entity.getProductName());
            item.setProductCategory(entity.getProductCategory());
            item.setSeat(entity.getSeat());
            item.setMonths(entity.getMonths());
            item.setStartDate(entity.getStartDate());
            item.setEndDate(entity.getEndDate());
            item.setAmount(centsToYuan(entity.getAmount()));
            items.add(item);
        });
        return items;
    }

    private List<BillingOrderPayment> ofPaymentEntities(List<OrderPaymentEntity> entities) {
        List<BillingOrderPayment> items = new ArrayList<>();
        entities.forEach(entity -> {
            BillingOrderPayment payment = new BillingOrderPayment();
            payment.setOrderId(entity.getOrderId());
            payment.setPaymentTransactionId(entity.getPaymentTransactionId());
            payment.setAmount(centsToYuan(entity.getAmount()));
            payment.setPayChannel(entity.getPayChannel());
            payment.setPayChannelTransactionId(entity.getPayChannelTransactionId());
            payment.setPaidTime(entity.getPaidTime());
            payment.setRawData(entity.getRawData());
            items.add(payment);
        });
        return items;
    }
}
