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

package com.apitable.enterprise.social.autoconfigure.feishu;

import java.util.HashMap;
import java.util.Map;

import com.vikadata.social.feishu.FeishuCardActionHandler;
import com.vikadata.social.feishu.FeishuEventCallbackHandler;
import com.vikadata.social.feishu.FeishuV3ContactEventCallbackHandler;
import com.vikadata.social.feishu.event.BaseEvent;
import com.vikadata.social.feishu.event.contact.v3.BaseV3ContactEvent;

/**
 * Event listener factory class
 * @author Shawn Deng
 */
public class EventListenerFactory {

    /**
     * old Event -> callbackHandler implementation
     */
    private final Map<Class<? extends BaseEvent>, FeishuEventCallbackHandler> eventHandlerMap = new HashMap<>(16);

    /**
     * v3 event
     */
    private final Map<Class<? extends BaseV3ContactEvent>, FeishuV3ContactEventCallbackHandler> v3ContactEventHandlerMap = new HashMap<>(16);

    /**
     * Message card event handler
     */
    private FeishuCardActionHandler cardEventHandler;

    public void addEventCallbackHandler(Map<Class<? extends BaseEvent>, FeishuEventCallbackHandler> eventHandlerMap) {
        this.eventHandlerMap.putAll(eventHandlerMap);
    }

    public void addV3ContactEventCallbackHandler(Map<Class<? extends BaseV3ContactEvent>, FeishuV3ContactEventCallbackHandler> eventHandlerMap) {
        this.v3ContactEventHandlerMap.putAll(eventHandlerMap);
    }

    public Map<Class<? extends BaseEvent>, FeishuEventCallbackHandler> getEventHandlerMap() {
        return eventHandlerMap;
    }

    public Map<Class<? extends BaseV3ContactEvent>, FeishuV3ContactEventCallbackHandler> getV3ContactEventHandlerMap() {
        return v3ContactEventHandlerMap;
    }

    public FeishuCardActionHandler getCardEventHandler() {
        return cardEventHandler;
    }

    public void setCardEventHandler(FeishuCardActionHandler cardEventHandler) {
        this.cardEventHandler = cardEventHandler;
    }
}
