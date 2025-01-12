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

package com.apitable.enterprise.k11.service;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.k11.infrastructure.K11Connector;
import com.apitable.enterprise.k11.infrastructure.model.SsoAuthInfo;
import com.apitable.shared.context.SessionContext;
import com.apitable.user.service.IUserService;
import com.apitable.core.exception.BusinessException;
import com.apitable.user.entity.UserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * connector template
 * </p>
 *
 * @author Chambers
 */
@Slf4j
@Component
public class K11Service {

    @Autowired(required = false)
    private K11Connector k11Connector;

    @Resource
    private IUserService iUserService;

    public void loginBySsoToken(String token) {
        log.info("sso token 「{}」Login", token);
        if (k11Connector != null) {
            try {
                SsoAuthInfo authInfo = k11Connector.loginBySsoToken(token);
                // The phone number may be a landline, so automatic binding is not required
                UserEntity user = iUserService.getByEmail(authInfo.getEmailAddr());
                if (user != null) {
                    iUserService.updateLoginTime(user.getId());
                    SessionContext.setUserId(user.getId());
                    return;
                }
                Long registerUserId = iUserService.create(null, null,
                    authInfo.getUserDisplayName(), null, authInfo.getEmailAddr(), "");
                SessionContext.setUserId(registerUserId);
                return;
            } catch (Exception e) {
                log.error("sso token「{}」login error, msg:{}", token, e.getMessage());
                throw new BusinessException("SSO login error");
            }
        }
        throw new BusinessException("This environment does not support SSO login");

    }

    public Long loginBySso(String username, String credential) {
        log.info("user「{}」login", username);
        if (k11Connector != null) {
            try {
                SsoAuthInfo authInfo = k11Connector.loginBySso(username, credential);
                UserEntity user = iUserService.getByEmail(authInfo.getEmailAddr());
                if (user != null) {
                    iUserService.updateLoginTime(user.getId());
                    return user.getId();
                }
                return iUserService.create(null, null,
                    authInfo.getUserDisplayName(), null, authInfo.getEmailAddr(), "");
            } catch (Exception e) {
                log.error("sso user「{}」login error, msg:{}", username, e.getMessage());
                throw new BusinessException("SSO login error");
            }
        }
        throw new BusinessException("This environment does not support SSO login");
    }

    public void sendSms(String target, String code) {
        if (k11Connector != null) {
            try {
                k11Connector.sendSms(target, code);
            } catch (Exception e) {
                throw new BusinessException("send sms message error");
            }
            return;
        }
        throw new BusinessException("SMS service is not enabled");
    }
}
