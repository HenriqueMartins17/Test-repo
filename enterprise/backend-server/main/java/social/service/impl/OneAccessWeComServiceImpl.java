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

package com.apitable.enterprise.social.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpOAuth2Service;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpOAuth2ServiceImpl;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;

import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.enterprise.social.service.IOneAccessWeComService;
import com.apitable.core.exception.BusinessException;

import org.springframework.stereotype.Service;

/**
 * OneAccess Wecom service
 */
@Service
public class OneAccessWeComServiceImpl implements IOneAccessWeComService {

    @Resource
    private OneAccessProperties oneAccessProperties;

    private WxCpOAuth2Service wxCpOAuth2Service;

    @PostConstruct
    public void init() {
        if (!oneAccessProperties.isEnabled() || oneAccessProperties.getWeCom() == null) {
            return;
        }
        WxCpDefaultConfigImpl wxCpConfigStorage = new WxCpDefaultConfigImpl();
        wxCpConfigStorage.setBaseApiUrl(oneAccessProperties.getWeCom().getBaseApiUrl());
        wxCpConfigStorage.setCorpId(oneAccessProperties.getWeCom().getCorpid());
        wxCpConfigStorage.setCorpSecret(oneAccessProperties.getWeCom().getSecret());
        wxCpConfigStorage.setAgentId(oneAccessProperties.getWeCom().getAgentId());
        WxCpService wxCpService = new WxCpServiceImpl();
        wxCpService.setWxCpConfigStorage(wxCpConfigStorage);
        wxCpOAuth2Service = new WxCpOAuth2ServiceImpl(wxCpService);
    }


    @Override
    public String getUserIdByOAuth2Code(String code) {
        try {
            return wxCpOAuth2Service.getUserInfo(code).getUserId();
        } catch (WxErrorException e) {
            WxError error = e.getError();
            if (null != error) {
                if (error.getErrorCode() == 40029) {
                    throw new BusinessException("Invalid CODE encoding");
                }
            }
            throw new RuntimeException(
                "Abnormality in Obtaining User Information of  WeCom Application：", e);
        }
    }
}
