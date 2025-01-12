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

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventHandler;
import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventListener;
import com.vikadata.social.dingtalk.event.CheckUrlEvent;

/**
 * <p>
 * DingTalk
 * Event Subscription - Basic Event
 * </p>
 */
@DingTalkEventHandler
@Slf4j
public class DingTalkAppEventHandler {


    /**
     * User Activation
     *
     * @param event Event content
     * @return Response content
     */
    @DingTalkEventListener
    public Object onCheckUrl(String agentId, CheckUrlEvent event) {
        // The event push of DingTalk will not repeat
        return "";
    }
}
