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

package com.apitable.enterprise.k11.controller;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.enterprise.k11.service.K11Service;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.config.properties.ConstProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * k11 Login interface.
 */
@RestController
@Tag(name = "k11 Login interface")
@ApiResource(path = "/k11")
@Slf4j
public class K11Controller {

    @Autowired
    private K11Service k11Service;

    @Resource
    private ConstProperties constProperties;

    /**
     * k11 Synchronous login with token.
     */
    @GetResource(path = "/oss/sync-login", requiredLogin = false)
    @Operation(summary = "k11 Synchronous login with token")
    public ResponseData<String> loginBySsoToken(
        @RequestParam(name = "token") String token, HttpServletResponse response)
        throws IOException {
        if (StrUtil.isEmptyIfStr(token)) {
            return ResponseData.success("SSO token cannot be empty");
        }
        String defaultWorkUri = constProperties.getServerDomain() + "/workbench";
        HttpSession session = HttpContextUtil.getSession(false);
        if (session != null) {
            response.sendRedirect(defaultWorkUri);
            return ResponseData.success(null);
        }
        try {
            k11Service.loginBySsoToken(token);
            response.sendRedirect(defaultWorkUri);
        } catch (Exception e) {
            return ResponseData.success(e.getMessage());
        }
        return ResponseData.success(null);
    }
}
