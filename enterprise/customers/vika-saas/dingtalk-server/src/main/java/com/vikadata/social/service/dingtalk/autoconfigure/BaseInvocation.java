/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.social.service.dingtalk.autoconfigure;

import java.lang.reflect.Method;

import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventHandler;

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
