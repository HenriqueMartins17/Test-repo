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

package com.apitable.enterprise.social.service;

import java.time.LocalDateTime;
import java.util.List;

import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;

/**
 * <p>
 * WeCom Service Provider Interface License
 * </p>
 */
public interface ISocialCpIsvPermitService {

    /**
     * Create a new order for the enterprise
     *
     * @param spaceId Space ID to purchase interface license
     * @param durationMonths The number of months for purchase is 31 days per month. Buy up to 36 months.(If the enterprise is a service provider testing enterprise, it can be purchased for 1 month at most)
     * @return Order Information
     */
    SocialWecomPermitOrderEntity createNewOrder(String spaceId, Integer durationMonths);

    /**
     * Activate the account in the interface license order
     *
     * @param orderId Interface license order number
     */
    void activateOrder(String orderId);

    /**
     * Renew the specified WeCom user
     *
     * @param spaceId Space ID
     * @param cpUserIds List of WeCom user IDs to renew
     * @param durationMonths The number of renewal months is 31 days per month. Buy up to 36 months. (If the enterprise is a service provider testing enterprise, each renewal can only be renewed for 1 month)
     * @return Order Information
     */
    SocialWecomPermitOrderEntity renewalCpUser(String spaceId, List<String> cpUserIds, Integer durationMonths);

    /**
     * Confirm the latest information of the order and all accounts under the enterprise
     *
     * @param orderId Interface license order number
     */
    void ensureOrderAndAllActiveCodes(String orderId);

    /**
     * Calculate the number of new purchase accounts
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @param spaceId Space ID
     * @return Number of accounts to be purchased
     */
    int calcNewAccountCount(String suiteId, String authCorpId, String spaceId);

    /**
     * Confirm the latest information of the interface license order, and update it if necessary
     *
     * @param orderId Interface license order number
     * @return Latest interface license Order Information
     */
    SocialWecomPermitOrderEntity ensureOrder(String orderId);

    /**
     * Confirm the latest information of all accounts under the enterprise
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     */
    void ensureAllActiveCodes(String suiteId, String authCorpId);

    /**
     * Automatic processing of interface licensing orders
     *
     * <p>
     * New purchase, renewal, or neglect
     * </p>
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @param spaceId Space ID
     */
    void autoProcessPermitOrder(String suiteId, String authCorpId, String spaceId);

    /**
     * Create an order for new purchase or renewal
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @param spaceId Space ID
     * @param expireTime Expiration time of paid subscription
     * @return Whether new purchase or renewal is required
     */
    boolean createPermitOrder(String suiteId, String authCorpId, String spaceId, LocalDateTime expireTime);

    /**
     * Send new Order Information to group robots
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @param spaceId Space id
     * @param orderId Order number. If it is not blank, send the ordered message; if it is blank, send the manual order message
     * @param durationMonths Number of months purchased. No need if the order has been placed
     * @return Send successfully
     */
    boolean sendNewWebhook(String suiteId, String authCorpId, String spaceId, String orderId, Integer durationMonths);

    /**
     * Send renewal Order Information to the swarm robot
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @param spaceId Space ID
     * @param orderId Order number. If it is not blank, send the ordered message; if it is blank, send the manual order message
     * @return Send successfully
     */
    boolean sendRenewWebhook(String suiteId, String authCorpId, String spaceId, String orderId);

    /**
     * Send refund information to group robots
     *
     * @param suiteId Application Suit ID
     * @param authCorpId Authorized enterprise ID
     * @return Send successfully
     */
    boolean sendRefundWebhook(String suiteId, String authCorpId);

}
