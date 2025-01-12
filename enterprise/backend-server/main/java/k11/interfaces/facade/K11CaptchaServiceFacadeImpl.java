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

package com.apitable.enterprise.k11.interfaces.facade;

import com.apitable.enterprise.k11.service.K11Service;
import com.apitable.interfaces.security.facade.CaptchaServiceFacade;
import com.apitable.interfaces.security.model.CaptchaReceiver;

public class K11CaptchaServiceFacadeImpl implements CaptchaServiceFacade {

    private final K11Service k11Service;

    public K11CaptchaServiceFacadeImpl(K11Service k11Service) {
        this.k11Service = k11Service;
    }

    @Override
    public void sendCaptcha(CaptchaReceiver receiver) {
        k11Service.sendSms(receiver.getReceiver(), receiver.getCaptchaCode());
    }
}
