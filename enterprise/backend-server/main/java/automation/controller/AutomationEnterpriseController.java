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

package com.apitable.enterprise.automation.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.automation.model.AutomationSchedulePushRO;
import com.apitable.enterprise.automation.service.IAutomationTriggerScheduleService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Automation - Enterprise API")
@ApiResource(path = "/automation")
public class AutomationEnterpriseController {

    @Resource
    private IAutomationTriggerScheduleService iAutomationTriggerScheduleService;

    @PostResource(path = "/schedule/push", requiredPermission = false)
    @Operation(description = "Push schedule to mq")
    public ResponseData<Void> pushScheduleMessage(@RequestBody @Valid AutomationSchedulePushRO data) {
        if (data.getPushAll()) {
            iAutomationTriggerScheduleService.publishAllTriggerScheduleWithoutVerify();
        } else if (!data.getScheduleIds().isEmpty()) {
            iAutomationTriggerScheduleService.publishTriggerSchedules(data.getScheduleIds());
        }
        return ResponseData.success();
    }
}
