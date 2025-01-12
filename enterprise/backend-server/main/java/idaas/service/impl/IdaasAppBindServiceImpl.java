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

package com.apitable.enterprise.idaas.service.impl;

import java.util.Objects;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.idaas.autoconfigure.IdaasProperties;
import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.model.WellKnowResponse;

import com.apitable.enterprise.idaas.mapper.IdaasAppBindMapper;
import com.apitable.enterprise.idaas.model.IdaasAppBindRo;
import com.apitable.enterprise.idaas.model.IdaasAppBindVo;
import com.apitable.enterprise.idaas.service.IIdaasAppBindService;
import com.apitable.enterprise.idaas.service.IIdaasAppService;
import com.apitable.enterprise.idaas.service.IIdaasAuthService;
import com.apitable.enterprise.idaas.service.IIdaasTenantService;
import com.apitable.enterprise.idaas.enums.IdaasException;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.idaas.entity.IdaasAppBindEntity;
import com.apitable.enterprise.idaas.entity.IdaasAppEntity;
import com.apitable.enterprise.idaas.entity.IdaasTenantEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * IDaaS application is bound to the space
 * </p>
 */
@Slf4j
@Service
public class IdaasAppBindServiceImpl extends ServiceImpl<IdaasAppBindMapper, IdaasAppBindEntity>
    implements IIdaasAppBindService {

    @Autowired(required = false)
    private IdaasProperties idaasProperties;

    @Autowired(required = false)
    private IdaasTemplate idaasTemplate;

    @Resource
    private IIdaasAppService idaasAppService;

    @Resource
    private IIdaasAuthService idaasAuthService;

    @Resource
    private IIdaasTenantService idaasTenantService;

    @Override
    public IdaasAppBindEntity getBySpaceId(String spaceId) {
        return getBaseMapper().selectBySpaceId(spaceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IdaasAppBindVo bindTenantApp(IdaasAppBindRo request) {
        IdaasTenantEntity tenantEntity =
            idaasTenantService.getByTenantName(request.getTenantName());
        if (Objects.isNull(tenantEntity)) {
            // Tenant information does not exist
            throw new BusinessException(IdaasException.PARAM_INVALID);
        }
        // 1 Call the Well known interface to obtain relevant information
        WellKnowResponse wellKnowResponse;
        try {
            wellKnowResponse = idaasTemplate.getSystemApi()
                .fetchWellKnown(request.getAppWellKnown());
        } catch (IdaasApiException ex) {
            log.error("Failed to fetch Well-known response.", ex);

            throw new BusinessException(IdaasException.API_ERROR);
        }
        // 2 Save or update application information
        String clientId = request.getAppClientId();
        IdaasAppEntity appEntity = idaasAppService.getByClientId(clientId);
        if (Objects.isNull(appEntity)) {
            // If the application information does not exist, create an application
            appEntity = IdaasAppEntity.builder()
                .tenantName(tenantEntity.getTenantName())
                .clientId(clientId)
                .clientSecret(request.getAppClientSecret())
                .authorizationEndpoint(wellKnowResponse.getAuthorizationEndpoint())
                .tokenEndpoint(wellKnowResponse.getTokenEndpoint())
                .userinfoEndpoint(wellKnowResponse.getUserinfoEndpoint())
                .build();
            idaasAppService.save(appEntity);
        } else {
            // If an app already exists, update the information
            appEntity.setTenantName(tenantEntity.getTenantName());
            appEntity.setClientId(clientId);
            appEntity.setClientSecret(request.getAppClientSecret());
            appEntity.setAuthorizationEndpoint(wellKnowResponse.getAuthorizationEndpoint());
            appEntity.setTokenEndpoint(wellKnowResponse.getTokenEndpoint());
            appEntity.setUserinfoEndpoint(wellKnowResponse.getUserinfoEndpoint());
            idaasAppService.updateById(appEntity);
        }
        // 3 Save or update binding information
        IdaasAppBindEntity appBindEntity = getBySpaceId(request.getSpaceId());
        if (Objects.isNull(appBindEntity)) {
            // If the information does not exist, create a binding relationship
            appBindEntity = IdaasAppBindEntity.builder()
                .tenantName(tenantEntity.getTenantName())
                .clientId(clientId)
                .spaceId(request.getSpaceId())
                .build();
            save(appBindEntity);
        } else {
            // If binding already exists, update the information
            appBindEntity.setTenantName(tenantEntity.getTenantName());
            appBindEntity.setClientId(clientId);
            appBindEntity.setSpaceId(request.getSpaceId());
            updateById(appBindEntity);
        }

        return IdaasAppBindVo.builder()
            .initiateLoginUri(idaasAuthService.getVikaLoginUrl(clientId))
            .redirectUri(idaasAuthService.getVikaCallbackUrl(clientId,
                idaasProperties.isSelfHosted() ? null : request.getSpaceId()))
            .build();
    }

}
