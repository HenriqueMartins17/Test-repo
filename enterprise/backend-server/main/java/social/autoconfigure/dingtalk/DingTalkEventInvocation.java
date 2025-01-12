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

package com.apitable.enterprise.social.autoconfigure.dingtalk;

import java.lang.reflect.Method;

import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventListener;


/** 
* <p> 
* wrapper class of event listener
* </p> 
* @author zoe zheng 
*/
public class DingTalkEventInvocation extends BaseInvocation {

    private DingTalkEventListener eventListenerAnnotation;

    private Class<?> eventType;


    public DingTalkEventInvocation(Method method, Object o, Class<?> eventType) {
        super(method, o);
        this.eventType = eventType;
        this.eventListenerAnnotation = method.getAnnotation(DingTalkEventListener.class);
    }

    public Class<?> getEventType() {
        return eventType;
    }
}
