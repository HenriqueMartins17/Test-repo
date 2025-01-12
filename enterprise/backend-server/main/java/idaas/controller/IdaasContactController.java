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
import com.apitable.enterprise.idaas.service.IIdaasContactService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * IDaaS address book.
 * </p>
 */
@Slf4j
@Tag(name = "IDaaS address book")
@RestController
@ApiResource(path = "/idaas/contact")
public class IdaasContactController {

    @Resource
    private IIdaasContactService idaasContactService;

    /**
     * Synchronize address book.
     */
    @PostResource(path = "/sync")
    @Operation(summary = "Synchronize address book")
    public ResponseData<Void> postSync() {
        idaasContactService.syncContact(LoginContext.me().getSpaceId(), SessionContext.getUserId());

        return ResponseData.success();
    }

}
