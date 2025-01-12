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

package com.apitable.enterprise.vikabilling.strategy;

import com.apitable.enterprise.vikabilling.model.SocialOrderContext;
import com.apitable.enterprise.vikabilling.enums.OrderChannel;

import org.springframework.beans.factory.InitializingBean;

/**
 * <p>
 * third party orders service interface
 * T order event
 * R refund order event
 * </p>
 */
public interface ISocialOrderService<T, R> extends InitializingBean {

    /**
     * retrieve order paid event
     *
     * @param event order event data
     * @return Order ID
     */
    String retrieveOrderPaidEvent(T event);

    /**
     * retrieve order paid event
     *
     * @param event refund order event data
     */
    void retrieveOrderRefundEvent(R event);

    /**
     * Migrate old data
     *
     * @param spaceId space id
     */
    void migrateEvent(String spaceId);

    /**
     * Build a unified social order context
     *
     * @param event social order event
     * @return SocialOrderContext
     */
    SocialOrderContext buildSocialOrderContext(T event);

    /**
     * Create order
     *
     * @param orderContext order content
     * @return order id
     */
    String createOrder(SocialOrderContext orderContext);

    /**
     * Create order detail
     *
     * @param orderContext      order content
     * @param subscriptionId    subscription id
     */
    void createOrderItem(String orderId, String subscriptionId, SocialOrderContext orderContext);

    /**
     * Create order metadata
     *
     * @param orderId       order id
     * @param orderChannel  order channel
     * @param event         order metadata
     */
    void createOrderMetaData(String orderId, OrderChannel orderChannel, T event);

    /**
     * Create subscription bundle
     *
     * @param orderContext order content
     * @return bundle id
     */
    String createBundle(SocialOrderContext orderContext);

    /**
     * Create subscription
     *
     * @param bundleId      bundle id
     * @param orderContext  order content
     * @return order id
     */
    String createSubscription(String bundleId, SocialOrderContext orderContext);

    /**
     * Renewal space subscription collection
     *
     * @param orderContext order content
     */
    String renewSubscription(SocialOrderContext orderContext);

    /**
     * Upgrade space subscription collection
     *
     * @param orderContext order content
     */
    String upgradeSubscription(SocialOrderContext orderContext);

    /**
     * Update the end time of the space collection
     *
     * @param orderContext order content
     */
    void updateBundleEndDate(SocialOrderContext orderContext);
}
