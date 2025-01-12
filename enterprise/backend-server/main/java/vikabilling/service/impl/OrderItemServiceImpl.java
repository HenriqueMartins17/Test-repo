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

package com.apitable.enterprise.vikabilling.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.mapper.OrderItemMapper;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Order Item Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemEntity> implements IOrderItemService {

    @Override
    public List<OrderItemEntity> getByOrderId(String orderId) {
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    public OrderItemEntity getBaseProductInOrder(String orderId) {
        List<OrderItemEntity> orderItemEntities = getByOrderId(orderId);
        return orderItemEntities.stream().filter(orderItem -> ProductCategory.BASE.name().equals(orderItem.getProductCategory()))
                .findFirst().orElse(null);
    }

    @Override
    public List<OrderItemEntity> getBySubscriptionId(String subscriptionId) {
        return baseMapper.selectBySubscriptionId(subscriptionId);
    }

    @Override
    public List<String> getSubscriptionIdsByOrderId(String orderId) {
        return baseMapper.selectSubscriptionIdsByOrderId(orderId);
    }
}
