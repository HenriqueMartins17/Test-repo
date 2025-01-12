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

package com.apitable.enterprise.social.event.feishu.v2;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.service.IFeishuEventService;
import com.apitable.enterprise.social.service.IFeishuInternalEventService;
import com.apitable.enterprise.social.service.ISocialFeishuEventLogService;
import com.apitable.enterprise.social.autoconfigure.feishu.annotation.FeishuEventHandler;
import com.apitable.enterprise.social.autoconfigure.feishu.annotation.FeishuEventListener;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptCreateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactScopeUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserUpdateEvent;

/**
 * Lark
 * Event subscription - new address book event
 */
@Slf4j
@FeishuEventHandler
public class FeishuV3ContactEventHandler {

    @Resource
    private ISocialFeishuEventLogService iSocialFeishuEventLogService;

    @Resource
    private IFeishuEventService iFeishuEventService;

    @Resource
    private IFeishuInternalEventService iFeishuInternalEventService;

    @FeishuEventListener
    public Object onContactScopeChangeEvent(ContactScopeUpdateEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleContactScopeChangeEvent(event);
        } else {
            iFeishuEventService.handleContactScopeChangeEvent(event);
        }
        return "";
    }

    @FeishuEventListener
    public Object onUserDeleteEvent(ContactUserDeleteEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleUserLeaveEvent(event);
        } else {
            iFeishuEventService.handleUserLeaveEvent(event);
        }
        return "";
    }

    @FeishuEventListener
    public Object onUserUpdateEvent(ContactUserUpdateEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleUserUpdateEvent(event);
        } else {
            iFeishuEventService.handleUserUpdateEvent(event);
        }
        return "";
    }

    @FeishuEventListener
    public Object onDeptCreateEvent(ContactDeptCreateEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleDeptCreateEvent(event);
        } else {
            iFeishuEventService.handleDeptCreateEvent(event);
        }
        return "";
    }

    @FeishuEventListener
    public Object onDeptDeleteEvent(ContactDeptDeleteEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleDeptDeleteEvent(event);
        } else {
            iFeishuEventService.handleDeptDeleteEvent(event);
        }
        return "";
    }

    @FeishuEventListener
    public Object onDeptUpdateEvent(ContactDeptUpdateEvent event) {
        iSocialFeishuEventLogService.createV3ContactEventLog(event);
        if (event.getAppInstanceId() != null) {
            iFeishuInternalEventService.handleDeptUpdateEvent(event);
        } else {
            iFeishuEventService.handleDeptUpdateEvent(event);
        }
        return "";
    }
}
