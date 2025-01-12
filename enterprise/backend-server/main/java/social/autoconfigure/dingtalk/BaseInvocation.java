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

import com.apitable.enterprise.social.autoconfigure.dingtalk.annotation.DingTalkEventHandler;

/**
 * <p> 
 * Annotation abstract base class
 * Provide the integration of {@link DingTalkEventHandler}
 * All inherited subclasses have the necessary basic annotation
 * </p> 
 * @author zoe zheng 
 */
public abstract class BaseInvocation {

    private Method method;

    private Object object;

    private DingTalkEventHandler handlerAnnotation;

    public BaseInvocation(Method method, Object object) {
        this.method = method;
        this.object = object;
        this.handlerAnnotation = object.getClass().getAnnotation(DingTalkEventHandler.class);
    }

    public Method getMethod() {
        return method;
    }

    public Object getObject() {
        return object;
    }
}
