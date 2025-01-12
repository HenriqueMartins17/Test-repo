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

package com.apitable.enterprise.security.service.impl;

import static com.apitable.base.enums.ActionException.ENABLE_SMS_VERIFICATION;
import static com.apitable.base.enums.ActionException.MAN_MACHINE_VERIFICATION_FAILED;
import static com.apitable.base.enums.ActionException.SECONDARY_VERIFICATION;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.security.afs.core.AfsChecker;
import com.apitable.enterprise.security.service.AfsCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Alibaba Cloud Shield Human-Machine Verification Interface Implementation Class
 * </p>
 *
 * @author Chambers
 */
@Slf4j
@Service
public class AfsCheckServiceImpl implements AfsCheckService {

    @Autowired(required = false)
    private AfsChecker afsChecker;

    @Value("${LOGIN_TOKEN:BornForFuture,FutureIsComing,TheyAreTheFuture}")
    private String loginToken;

    @Override
    public boolean getEnabledStatus() {
        return afsChecker != null;
    }

    @Override
    public void noTraceCheck(String data) {
        if (afsChecker == null) {
            log.info("man machine authentication is not enabled");
            return;
        }
        if (StrUtil.isBlank(data)) {
            throw new BusinessException(MAN_MACHINE_VERIFICATION_FAILED);
        } else if (loginToken != null && loginToken.contains(data)) {
            return;
        }
        String scoreJsonStr =
            "{\"200\":\"PASS\",\"400\":\"NC\",\"600\":\"NC\",\"700\":\"NC\",\"800\":\"BLOCK\"}";
        String result = afsChecker.noTraceCheck(data, scoreJsonStr);
        log.info("human machine verification results:{}", result);
        ExceptionUtil.isNotNull(result, SECONDARY_VERIFICATION);
        switch (result) {
            case "100":
            case "200":
                // directly through
                break;
            case "400":
            case "600":
            case "700":
                // evoke slider captcha
                throw new BusinessException(SECONDARY_VERIFICATION);
            case "800":
                // authentication failed directly intercept
                throw new BusinessException(MAN_MACHINE_VERIFICATION_FAILED);
            case "900":
                // After the slider verification continues to be identified as illegal by risk control,
                // enable SMS verification code verification
                throw new BusinessException(ENABLE_SMS_VERIFICATION);
            default:
                break;
        }
    }
}
