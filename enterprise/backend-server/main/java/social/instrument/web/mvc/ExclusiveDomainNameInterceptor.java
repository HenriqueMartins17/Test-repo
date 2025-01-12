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

package com.apitable.enterprise.social.instrument.web.mvc;

import java.util.Collections;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.TenantDomainStatus;
import com.apitable.enterprise.social.model.SpaceBindDomainDTO;
import com.apitable.enterprise.social.model.SpaceBindTenantInfoDTO;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDomainService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.shared.component.ResourceDefinition;
import com.apitable.shared.component.scanner.ApiResourceFactory;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.interceptor.AbstractServletSupport;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.HttpContextUtil;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.apitable.auth.enums.AuthException.NONE_RESOURCE;
import static com.apitable.enterprise.social.enums.SocialException.EXCLUSIVE_DOMAIN_UNBOUND;
import static com.apitable.workspace.enums.PermissionException.NODE_ACCESS_DENIED;

/**
 * Exclusive domain name blocker
 *
 * <p>Intercept the currently used domain name, whether it is the bound space
 *
 * @author Pengap
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression(value = "${social.wecom.enabled:false} and ${social.wecom.check-domain:true}")
public class ExclusiveDomainNameInterceptor extends AbstractServletSupport
    implements HandlerInterceptor {

    @Resource
    private ApiResourceFactory apiResourceFactory;

    @Resource
    private ISocialTenantDomainService iSocialTenantDomainService;

    private final String[] IGNORE_CHECK_DOMAIN_URL = {"/user/me", "/social/wecom/bind/(.*)/config"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String requestPath = resolveServletPath(request);
        for (String ignoreItem : IGNORE_CHECK_DOMAIN_URL) {
            if (ReUtil.isMatch(ignoreItem, requestPath)) {
                return true;
            }
        }
        ResourceDefinition resourceDef =
            apiResourceFactory.getResourceByUrl(requestPath, request.getMethod());
        if (resourceDef == null) {
            throw new BusinessException(NONE_RESOURCE);
        }

        SpaceBindDomainDTO dto;
        String remoteHost = HttpContextUtil.getRemoteHost(request);
        String spaceId = request.getHeader(ParamsConstants.SPACE_ID);

        if (resourceDef.getRequiredAccessDomain()) {
            // 1.Check the availability of the domain name first
            dto = iSocialTenantDomainService.getSpaceDomainByDomainName(remoteHost);
            if (null != dto) {
                if (TenantDomainStatus.WAIT_BIND.getCode() == dto.getStatus()) {
                    // If the domain name is prohibited from binding, and an error message pops up
                    throw new BusinessException(EXCLUSIVE_DOMAIN_UNBOUND);
                }
            }
            // 2.Check whether the access domain name matches the domain name bound to the space
            if (StrUtil.isNotBlank(spaceId)) {
                dto = CollUtil.getFirst(iSocialTenantDomainService.getSpaceDomainBySpaceIds(
                    Collections.singletonList(spaceId)));
                ExceptionUtil.isNotNull(dto, EXCLUSIVE_DOMAIN_UNBOUND);

                // If the space domain name is not available, return the public domain name directly
                String spaceDomain = dto.getDomainName();
                if (!StrUtil.equals(spaceDomain, remoteHost)) {
                    throw new BusinessException(NODE_ACCESS_DENIED);
                }
            }
        }
        return true;
    }
}
