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

import com.apitable.enterprise.social.service.impl.FeishuEventServiceImpl.SpaceContext;
import com.vikadata.social.feishu.event.app.AppOpenEvent;
import com.vikadata.social.feishu.event.app.AppStatusChangeEvent;
import com.vikadata.social.feishu.event.app.AppUninstalledEvent;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;
import com.vikadata.social.feishu.event.bot.AddBotEvent;
import com.vikadata.social.feishu.event.bot.BaseMessageEvent;
import com.vikadata.social.feishu.event.bot.P2pChatCreateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptCreateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactScopeUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserUpdateEvent;
import com.vikadata.social.feishu.model.v3.FeishuDeptObject;
import com.vikadata.social.feishu.model.v3.FeishuUserObject;
import org.springframework.util.MultiValueMap;

/**
 * Lark event processing service
 */
public interface IFeishuEventService {

    /**
     * Processing the first application
     *
     * @param event Event
     */
    void handleAppOpenEvent(AppOpenEvent event);

    /**
     * Processing application stop enable event
     *
     * @param event Event
     */
    void handleAppStopEvent(AppStatusChangeEvent event);

    /**
     * Processing application stop enable event
     *
     * @param event Event
     */
    void handleAppRestartEvent(AppStatusChangeEvent event);

    /**
     * Processing app uninstallation
     *
     * @param event Event
     */
    void handleAppUninstalledEvent(AppUninstalledEvent event);

    /**
     * Handle the change of address book authorization scope
     *
     * @param event Event
     */
    void handleContactScopeChangeEvent(ContactScopeUpdateEvent event);

    /**
     * Processing tenant address book data.
     *
     * @param appId        App ID
     * @param tenantKey    Tenant ID
     * @param spaceId      Space ID, maybe empty
     * @param contactMap   Tenant Address Book Data Structure
     * @param checkSeatNum whether to check seat count
     * @return SpaceContext
     */
    SpaceContext handleTenantContactData(String appId, String tenantKey, String spaceId,
                                         MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap,
                                         String operatorOpenId, boolean checkSeatNum);

    /**
     * Process employee resignation event
     *
     * @param event Event
     */
    void handleUserLeaveEvent(ContactUserDeleteEvent event);

    /**
     * Handling employee information change event
     *
     * @param event Event
     */
    void handleUserUpdateEvent(ContactUserUpdateEvent event);

    /**
     * New Processing Department
     *
     * @param event Event
     */
    void handleDeptCreateEvent(ContactDeptCreateEvent event);

    /**
     * Processing department is deleted
     *
     * @param event Event
     */
    void handleDeptDeleteEvent(ContactDeptDeleteEvent event);

    /**
     * Handling department information changes
     *
     * @param event Event
     */
    void handleDeptUpdateEvent(ContactDeptUpdateEvent event);

    /**
     * The session of processing user and robot is created for the first time
     *
     * @param event Event
     */
    void handleP2pChatCreateEvent(P2pChatCreateEvent event);

    /**
     * Event when processing robot is invited to join group chat
     *
     * @param event Event
     */
    void handleAddBotEvent(AddBotEvent event);

    /**
     * Process and receive robot message Event
     *
     * @param event Event
     */
    <E extends BaseMessageEvent> void handleMessageEvent(E event);

    /**
     * Process Order Payment Event
     *
     * @param event Event
     */
    void handleOrderPaidEvent(OrderPaidEvent event);

    /**
     * Process Lark Tenant Orders
     *
     * @param tenantKey Tenant ID
     * @param appId     App ID
     */
    void handleTenantOrders(String tenantKey, String appId);
}
