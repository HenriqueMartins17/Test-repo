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

package com.apitable.enterprise.social.instrument.web;

import com.apitable.enterprise.shared.annotation.VikaSaaSEditionCondition;
import com.apitable.enterprise.social.interfaces.facade.EnterpriseSocialServiceFacadeImpl;
import com.apitable.enterprise.social.service.IDingTalkDaService;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDomainService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.interfaces.social.facade.SocialServiceFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * social domain context config.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(VikaSaaSEditionCondition.class)
public class SocialDomainContextConfig {

    /**
     * init bean.
     */
    @Bean
    @Primary
    public SocialServiceFacade enterpriseSocialServiceFacade(
        ISocialTenantService socialTenantService, ISocialTenantBindService socialTenantBindService,
        ISocialUserBindService socialUserBindService, ISocialService socialService,
        IDingTalkDaService dingTalkDaService, IDingTalkInternalService dingTalkInternalService,
        ISocialTenantDomainService socialTenantDomainService,
        ISocialCpIsvService socialCpIsvService) {
        return new EnterpriseSocialServiceFacadeImpl(socialTenantService, socialTenantBindService,
            socialUserBindService, socialService, dingTalkDaService, dingTalkInternalService,
            socialTenantDomainService, socialCpIsvService);
    }
}
