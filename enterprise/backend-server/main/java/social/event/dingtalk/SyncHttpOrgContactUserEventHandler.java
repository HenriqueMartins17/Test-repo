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

package com.apitable.enterprise.social.event.dingtalk;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.service.IDingTalkIsvEventService;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventHandler;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventListener;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserActiveOrgEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserAddOrgEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserDeptChangeEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserLeaveOrgEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserModifyOrgEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserRoleChangeEvent;

import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * <p>
 * Event subscription -- general priority data, which is the latest status of enterprise employees
 * </p>
 */
@DingTalkEventHandler
@Slf4j
public class SyncHttpOrgContactUserEventHandler {
    @Resource
    private IDingTalkIsvEventService iDingTalkIsvEventService;

    /**
     * Employee information after the enterprise adds an employee event
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserAddOrgEvent(String userId, SyncHttpUserAddOrgEvent event) {
        log.info("Received DingTalk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        iDingTalkIsvEventService.handleUserAddOrgEvent(userId, event);
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Employee information after an enterprise modifies an employee event
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserModifyOrgEvent(String userId, SyncHttpUserModifyOrgEvent event) {
        log.info("Received DingTalk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        // Re joining the space station will push user modification events
        iDingTalkIsvEventService.handleUserAddOrgEvent(userId, event);
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Employee information after the event of modifying the employee's department
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserDeptChangeEvent(String userId, SyncHttpUserDeptChangeEvent event) {
        log.info("Received Ding]Talk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        iDingTalkIsvEventService.handleUserAddOrgEvent(userId, event);
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * Employee information after the enterprise modifies the employee's role (including administrator change) event
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserRoleChangeEvent(String userId, SyncHttpUserRoleChangeEvent event) {
        log.info("Received DingTalk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        // todo
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * The activation information after the user joins the enterprise. If the active field is true, it means it has been activated
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserActiveOrgEvent(String userId, SyncHttpUserActiveOrgEvent event) {
        log.info("Received DingTalk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        // The user activates the enterprise, which is equivalent to joining the enterprise, because when joining the enterprise, it is judged whether the user is activated
        iDingTalkIsvEventService.handleUserAddOrgEvent(userId, event);
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }

    /**
     * The activation information after the user joins the enterprise. If the active field is true, it means it has been activated
     *
     * @param userId Employee's userid
     * @param event  Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onUserLeaveOrgEvent(String userId, SyncHttpUserLeaveOrgEvent event) {
        log.info("Received DingTalk push event:[{}:{}]", event.getEventType(),
            event.getSyncAction());
        iDingTalkIsvEventService.handleUserLeaveOrgEvent(userId, event);
        // The event push of DingTalk will not repeat
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
