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

package com.apitable.enterprise.social.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.social.model.WoaAppBindSpaceRo;
import com.apitable.enterprise.social.model.WoaUserLoginRo;
import com.apitable.enterprise.social.model.WoaUserLoginVo;
import com.apitable.enterprise.social.service.IWoaService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Woa controller.
 *
 * @author penglong feng
 */
@RestController
@ApiResource(path = "/social/woa")
@Tag(name = "Third party platform integration interface -- Woa")
public class SocialWoaController {

    @Resource
    private IWoaService iWoaService;

    @PostResource(path = "/user/login", requiredLogin = false)
    @Operation(summary = "Woa Application User Login")
    public ResponseData<WoaUserLoginVo> userLogin(@RequestBody @Valid WoaUserLoginRo body) {
        WoaUserLoginVo view =
            iWoaService.userLoginByOAuth2Code(body.getAppId(), body.getCode());
        SessionContext.setUserId(view.getUserId());
        return ResponseData.success(view);
    }

    /**
     * Woa App Refresh Address Book.
     */
    @PostResource(path = "/refresh/contact", tags = "UPDATE_SPACE")
    @Operation(summary = "Woa App Refresh Address Book",
        description = "Apply to refresh the address book manually")
    @Parameter(name = ParamsConstants.SPACE_ID, description = "Space ID", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW")
    public ResponseData<Void> refreshContact() {
        Long userId = SessionContext.getUserId();
        String spaceId = LoginContext.me().getSpaceId();
        iWoaService.refreshContact(userId, spaceId);
        return ResponseData.success();
    }

    /**
     * Woa Application binding space.
     */
    @PostResource(path = "/bindSpace", requiredPermission = false)
    @Operation(summary = "Woa Application Binding Space")
    public ResponseData<Void> bindSpace(@RequestBody @Valid WoaAppBindSpaceRo body) {
        Long userId = SessionContext.getUserId();
        // Bind space
        iWoaService.bindSpace(userId, body.getSpaceId(), body.getAppId(), body.getSecretKey());
        return ResponseData.success();
    }
}
