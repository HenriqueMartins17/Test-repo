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

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.model.WxCpIsvPermanentCodeInfo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application authorization successfully processed
 * </p>
 */
@Service
public class SocialCpIsvInstallSelfAuthCreateEntityHandler
    implements ISocialCpIsvEntityHandler, InitializingBean {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ISocialCpIsvService socialCpIsvService;

    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType type() {

        return WeComIsvMessageType.INSTALL_SELF_AUTH_CREATE;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {

        WxCpIsvPermanentCodeInfo permanentCodeInfo =
            JSONUtil.toBean(unprocessed.getMessage(), WxCpIsvPermanentCodeInfo.class);
        socialCpIsvService.createAuthFromPermanentInfo(unprocessed.getSuiteId(),
            unprocessed.getAuthCorpId(), permanentCodeInfo);
        return true;

    }

    @Override
    public void afterPropertiesSet() {

        this.socialCpIsvMessageService =
            applicationContext.getBean(ISocialCpIsvMessageService.class);

    }

}
