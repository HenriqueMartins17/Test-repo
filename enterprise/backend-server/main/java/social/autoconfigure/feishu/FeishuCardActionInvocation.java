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

import java.lang.reflect.Method;

import com.apitable.enterprise.social.autoconfigure.feishu.annotation.FeishuCardActionListener;

/**
 * Message Card event call
 *
 * @author Shawn Deng
 */
public class FeishuCardActionInvocation extends BaseInvocation {

    private final FeishuCardActionListener cardActionAnnotation;

    public FeishuCardActionInvocation(Method method, Object o) {
        super(method, o);
        this.cardActionAnnotation = method.getAnnotation(FeishuCardActionListener.class);
    }

    public FeishuCardActionListener getCardActionAnnotation() {
        return cardActionAnnotation;
    }
}
