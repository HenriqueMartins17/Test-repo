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

package com.apitable.enterprise.gm.controller;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.gm.vo.WidgetInfoVo;
import com.apitable.widget.service.IWidgetPackageService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.widget.entity.WidgetPackageEntity;
import com.google.common.util.concurrent.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * An open interface, typically used for unimportant and ambiguous data.
 * </p>
 */
@RestController
@Tag(name = "Open Api")
@ApiResource(path = "/openapi")
public class OpenApiController {

    // Open api Use single machine current limiting mode.
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(5);

    @Resource
    private IWidgetPackageService iWidgetPackageService;

    /**
     * Get information that the applet can expose.
     */
    @GetResource(path = "/widgetInfo/{widgetId}", requiredPermission = false)
    @Operation(summary = "Get information that the applet can expose", description = "Get "
        + "information that the applet can expose")
    public ResponseData<WidgetInfoVo> validateApiKey(@PathVariable("widgetId") String widgetId) {
        if (RATE_LIMITER.tryAcquire()) {
            WidgetPackageEntity widget = iWidgetPackageService.getByPackageId(widgetId);
            return ResponseData.success(this.widgetInfoVoWrapper(widget));
        } else {
            throw new BusinessException("The interface is busy. Please try again later...");
        }
    }

    private WidgetInfoVo widgetInfoVoWrapper(WidgetPackageEntity origin) {
        return Optional.ofNullable(origin).map(widget -> {
            WidgetInfoVo widgetInfoVo = new WidgetInfoVo();
            widgetInfoVo.setWidgetName(widget.getI18nName());
            widgetInfoVo.setWidgetDescription(widget.getI18nDescription());
            return widgetInfoVo;
        }).orElse(null);
    }
}
