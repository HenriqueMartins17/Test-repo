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

package com.apitable.enterprise.vikabilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.vikabilling.model.EventVO;
import com.apitable.enterprise.vikabilling.setting.Event;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * Billing Event Api.
 */
@RestController
@Tag(name = "Billing Event Api")
@ApiResource
public class BillingEventController {

    /**
     * Fetch event list.
     */
    @GetResource(path = "/events/active", requiredPermission = false)
    @Operation(summary = "fetch event list")
    public ResponseData<EventVO> fetchEventList() {
        Event event =
            BillingConfigManager.getEventOnEffectiveDate(ClockManager.me().getLocalDateNow());
        return ResponseData.success(EventVO.of(event));
    }
}
