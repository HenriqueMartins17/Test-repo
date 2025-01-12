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

package com.apitable.enterprise.user.instrument.web;

import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.shared.annotation.VikaSaaSEditionCondition;
import com.apitable.enterprise.user.interfaces.facade.EnterpriseUserLinkServiceFacadeImpl;
import com.apitable.enterprise.user.interfaces.facade.EnterpriseUserServiceFacadeImpl;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.interfaces.user.facade.UserLinkServiceFacade;
import com.apitable.interfaces.user.facade.UserServiceFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * user service facade implements for vika saas.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(VikaSaaSEditionCondition.class)
public class UserDomainContextConfig {

    @Bean
    @Primary
    public UserServiceFacade enterpriseUserServiceFacade(IVCodeService codeService,
                                                         IIntegralService integralService) {
        return new EnterpriseUserServiceFacadeImpl(codeService, integralService);
    }

    @Bean
    @Primary
    public UserLinkServiceFacade enterpriseUserLinkServiceFacade(
        IUserLinkService userLinkService) {
        return new EnterpriseUserLinkServiceFacadeImpl(userLinkService);
    }
}
