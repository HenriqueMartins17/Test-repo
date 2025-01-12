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

import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.BaseOrgUserContactEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserLeaveOrgEvent;

/**
 * <p>
 * ISV DingTalk Event Service Interface.
 * </p>
 */
public interface IDingTalkIsvEventService {
    /**
     * Handle the DingTalk enterprise authorization event.
     *
     * @param suiteId Suit ID
     * @param event   Authorization Event
     */
    void handleOrgSuiteAuthEvent(String suiteId, BaseOrgSuiteEvent event);

    /**
     * Handle the DingTalk authorization change event.
     *
     * @param suiteId Suit ID
     * @param event   Authorization Event
     */
    void handleOrgSuiteChangeEvent(String suiteId, BaseOrgSuiteEvent event);

    /**
     * Process DingTalk Enterprise De authorization.
     *
     * @param suiteId Suit ID
     * @param corpId  Authorized enterprise ID
     */
    void handleOrgSuiteRelieveEvent(String suiteId, String corpId);

    /**
     * Process the DingTalk app enable event
     * Because this event can only be a background operation, it must have been manually disabled in the background before.
     *
     * @param suiteId Suit ID
     * @param corpId  Authorized enterprise ID
     */
    void handleOrgMicroAppRestoreEvent(String suiteId, String corpId);

    /**
     * Process the Ding Talk application deactivation event. This event can only be a background operation and can be re enabled, so the user information will not be deleted
     *
     * @param suiteId Suit ID
     * @param corpId  Authorized enterprise ID
     */
    void handleOrgMicroAppStopEvent(String suiteId, String corpId);

    /**
     * Process user joining the enterprise.
     *
     * @param openId User ID
     * @param event  Event
     */
    void handleUserAddOrgEvent(String openId, BaseOrgUserContactEvent event);

    /**
     * Handle user leaving the enterprise.
     *
     * @param openId User ID
     * @param event  Event
     */
    void handleUserLeaveOrgEvent(String openId, SyncHttpUserLeaveOrgEvent event);

    /**
     * Processing application market order information.
     *
     * @param event Order information
     * @return vika order id
     */
    String handleMarketOrderEvent(SyncHttpMarketOrderEvent event);

    /**
     * Process the order closing event.
     *
     * @param event Unsubscribe Order Information
     */
    void handleMarketServiceClosedEvent(SyncHttpMarketServiceCloseEvent event);
}
