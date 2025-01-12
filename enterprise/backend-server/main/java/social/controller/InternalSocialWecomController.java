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
import com.apitable.enterprise.social.autoconfigure.wecom.WeComProperties;
import com.apitable.enterprise.social.autoconfigure.wecom.WeComProperties.IsvApp;
import com.apitable.enterprise.social.service.ISocialWecomPermitDelayService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal Service-Enterprise Micro Interface.
 */
@RestController
@Tag(name = "Internal Service-Enterprise Micro Interface")
@ApiResource(path = "/internal/social/wecom")
public class InternalSocialWecomController {

    @Autowired(required = false)
    private WeComProperties weComProperties;

    @Resource
    private ISocialWecomPermitDelayService socialWecomPermitDelayService;

    /**
     * Batch processing pending interface license delay information.
     */
    @PostResource(path = "/permitDelay/batchProcess", requiredLogin = false)
    @Operation(summary = "Batch processing pending interface license delay information")
    public ResponseData<Void> postPermitDelayBatchProcess() {
        List<String> suiteIds = weComProperties.getIsvAppList().stream()
            .map(IsvApp::getSuiteId)
            .collect(Collectors.toList());
        suiteIds.forEach(suiteId -> socialWecomPermitDelayService.batchProcessPending(suiteId));
        return ResponseData.success();
    }

}
