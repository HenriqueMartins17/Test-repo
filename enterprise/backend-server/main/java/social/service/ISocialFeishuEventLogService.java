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

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.entity.SocialFeishuEventLogEntity;
import com.vikadata.social.feishu.event.BaseEvent;
import com.vikadata.social.feishu.event.contact.v3.BaseV3ContactEvent;

/**
 * Third party platform integration - Mark event log service interface
 */
public interface ISocialFeishuEventLogService extends IService<SocialFeishuEventLogEntity> {

    /**
     * Event processing completed
     *
     * @param uuid Event Unique ID
     */
    void doneEvent(String uuid);

    /**
     * Create event record
     *
     * @param event Event
     */
    <T extends BaseEvent> boolean create(T event);

    /**
     * Create event record
     * New Address Book Event
     *
     * @param event Event
     */
    <T extends BaseV3ContactEvent> boolean createV3ContactEventLog(T event);
}
