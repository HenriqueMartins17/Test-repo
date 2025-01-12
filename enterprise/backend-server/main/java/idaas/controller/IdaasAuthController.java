/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.idaas.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.idaas.entity.IdaasAppBindEntity;
import com.apitable.enterprise.idaas.model.IdaasAuthCallbackRo;
import com.apitable.enterprise.idaas.model.IdaasAuthLoginVo;
import com.apitable.enterprise.idaas.model.IdaasBindInfoVo;
import com.apitable.enterprise.idaas.service.IIdaasAppBindService;
import com.apitable.enterprise.idaas.service.IIdaasAuthService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Objects;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * IDaaS Login authorization.
 * </p>
 */
@Slf4j
@Tag(name = "IDaaS Login authorization")
@RestController
@ApiResource(path = "/idaas/auth")
public class IdaasAuthController {

    @Resource
    private IIdaasAppBindService idaasAppBindService;

    @Resource
    private IIdaasAuthService idaasAuthService;

    /**
     * Get the IDaaS information bound to the space.
     */
    @GetResource(path = "/{spaceId}/bindInfo", requiredLogin = false)
    @Operation(summary = "Get the IDaaS information bound to the space")
    public ResponseData<IdaasBindInfoVo> getBindInfo(@PathVariable("spaceId") String spaceId) {
        IdaasBindInfoVo idaasBindInfoVo = new IdaasBindInfoVo();
        IdaasAppBindEntity appBindEntity = idaasAppBindService.getBySpaceId(spaceId);
        if (Objects.isNull(appBindEntity)) {
            idaasBindInfoVo.setEnabled(false);
        } else {
            idaasBindInfoVo.setEnabled(true);
            idaasBindInfoVo.setClientId(appBindEntity.getClientId());
        }

        return ResponseData.success(idaasBindInfoVo);
    }

    @GetResource(path = "/login/{clientId}", requiredLogin = false)
    @Operation(summary = "Get the link to log in to the IDaaS system")
    public ResponseData<IdaasAuthLoginVo> getLogin(@PathVariable("clientId") String clientId) {
        return ResponseData.success(idaasAuthService.idaasLoginUrl(clientId));
    }

    /**
     * Jump to the IDaaS system for automatic login.
     */
    @GetResource(path = "/login/redirect/{clientId}", requiredLogin = false)
    @Operation(summary = "Jump to the IDaaS system for automatic login")
    public void getLoginRedirect(@PathVariable("clientId") String clientId,
                                 HttpServletResponse response) {
        IdaasAuthLoginVo idaasAuthLoginVo = idaasAuthService.idaasLoginUrl(clientId);

        try {
            response.sendRedirect(idaasAuthLoginVo.getLoginUrl());
        } catch (IOException ex) {
            log.warn("Failed to send redirect.", ex);
        }
    }

    /**
     * The user completes subsequent operations after logging in to the IDaaS.
     */
    @PostResource(path = "/callback/{clientId}", requiredLogin = false)
    @Operation(summary = "The user completes subsequent operations after logging in to the IDaaS "
        + "system", description = "For private deployment only")
    public ResponseData<Void> postCallback(@PathVariable("clientId") String clientId,
                                           @RequestBody IdaasAuthCallbackRo request) {
        idaasAuthService.idaasLoginCallback(clientId, null, request.getCode(), request.getState());

        return ResponseData.success();
    }

    /**
     * The user completes subsequent operations after logging in to the IDaaS.
     */
    @PostResource(path = "/callback/{clientId}/{spaceId}", requiredLogin = false)
    @Operation(summary = "The user completes subsequent operations after logging in to the IDaaS "
        + "system", description = "For Sass version only")
    public ResponseData<Void> postSpaceCallback(@PathVariable("clientId") String clientId,
                                                @PathVariable("spaceId") String spaceId,
                                                @RequestBody IdaasAuthCallbackRo request) {
        idaasAuthService.idaasLoginCallback(clientId, spaceId, request.getCode(),
            request.getState());

        return ResponseData.success();
    }

}
