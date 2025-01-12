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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.config.properties.ConstProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>
 * Third party platform integration interface.
 * </p>
 */
@RestController
@ApiResource(path = "/social")
@Tag(name = "Third party platform integration interface")
@Slf4j
public class SocialController {

    @Resource
    private ConstProperties constProperties;

    @Resource
    private ServerProperties serverProperties;

    @Resource
    private IFeishuService iFeishuService;

    /**
     * Lark configuration application callback.
     */
    @GetResource(path = "/feishu/workbench/callback", requiredLogin = false)
    @Operation(summary = "Lark configuration application callback", hidden = true)
    public void feishuWorkbenchCallback(@RequestParam(name = "url") String url,
                                        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        log.info("Lark callback received: {}, parameter：{}", request.getRequestURI(),
            request.getQueryString());
        String redirectUri = constProperties.getServerDomain()
            + serverProperties.getServlet().getContextPath()
            + StrUtil.format("/social/feishu/entry?url={}", url);
        response.sendRedirect(redirectUri);
    }

    /**
     * Lark configuration application callback.
     */
    @Deprecated
    @GetResource(path = "/feishu/configure/callback", requiredLogin = false)
    @Operation(summary = "Lark configuration application callback", hidden = true)
    public void feishuConfigureCallback(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        log.info("Lark callback received: {}, parameter：{}", request.getRequestURI(),
            request.getQueryString());
        // Redirect to the Getting Started portal
        String redirectUri = constProperties.getServerDomain()
            + serverProperties.getServlet().getContextPath()
            + "/social/feishu/admin";
        response.sendRedirect(redirectUri);
    }

    /**
     * Lark authorized login.
     */
    @GetResource(path = "/feishu/auth/callback", requiredLogin = false)
    @Operation(summary = "Lark authorized login", hidden = true)
    public RedirectView feishuAuthCallback() {
        iFeishuService.switchDefaultContext();
        String redirectUri;
        if (iFeishuService.isDefaultIsv()) {
            redirectUri =
                constProperties.getServerDomain() + serverProperties.getServlet().getContextPath()
                    + "/social/feishu/entry/callback";
        } else {
            redirectUri =
                constProperties.getServerDomain() + serverProperties.getServlet().getContextPath()
                    + "/social/feishu/login/callback";
        }
        return new RedirectView(
            iFeishuService.buildAuthUrl(redirectUri, String.valueOf(DateUtil.date().getTime())));
    }
}
