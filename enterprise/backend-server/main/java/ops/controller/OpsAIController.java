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

package com.apitable.enterprise.ops.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.gm.ro.ChatbotEnableRo;
import com.apitable.shared.cache.service.CommonCacheService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - AI API.
 * </p>
 */
@RestController
@ApiResource(path = "/")
@Tag(name = "Product Operation System - AI API")
public class OpsAIController {

    @Resource
    private CommonCacheService commonCacheService;

    @Value("${ENABLE_CHATBOT_TOKEN:K9vvkTLy2eaViE4BAjpuEYEs}")
    private String enableChatbotToken;

    @PostResource(path = "/chatbot/enable", requiredLogin = false)
    @Operation(summary = "Enable specified space chatbot feature")
    public ResponseData<Void> enableChatbot(@RequestBody ChatbotEnableRo ro) {
        if (!enableChatbotToken.equalsIgnoreCase(ro.getToken())) {
            return ResponseData.error("Token error.");
        }
        if (Boolean.TRUE.equals(ro.getIsOff())) {
            commonCacheService.delSpaceChatbotCache(ro.getSpaceId());
            return ResponseData.success();
        }
        commonCacheService.saveSpaceChatbotCache(ro.getSpaceId(), ro.getDays());
        return ResponseData.success();
    }

}
