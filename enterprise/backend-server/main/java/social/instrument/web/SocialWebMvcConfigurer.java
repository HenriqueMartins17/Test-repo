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

import static com.apitable.shared.config.WebMvcConfig.INTERCEPTOR_IGNORE_PATHS;

import com.apitable.enterprise.social.instrument.web.mvc.ExclusiveDomainNameInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ExclusiveDomainNameInterceptor.class)
class SocialWebMvcConfigurer implements WebMvcConfigurer {

    @Resource
    ApplicationContext applicationContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                this.applicationContext.getBean(ExclusiveDomainNameInterceptor.class))
            .excludePathPatterns(INTERCEPTOR_IGNORE_PATHS);
    }
}
