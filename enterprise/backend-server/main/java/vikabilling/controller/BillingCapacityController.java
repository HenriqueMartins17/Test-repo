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

import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.vikabilling.service.IBillingCapacityService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.apitable.space.vo.SpaceCapacityPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Billing Capacity Api.
 */
@RestController
@Tag(name = "Billing Capacity Api")
@ApiResource
@Slf4j
public class BillingCapacityController {

    @Resource
    private IBillingCapacityService iBillingCapacityService;

    /**
     * Get space capacity detail info.
     */
    @GetResource(path = "/space/capacity/detail", requiredPermission = false)
    @Operation(summary = "Get space capacity detail info")
    @Parameters({
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spczJrh2i3tLW"),
        @Parameter(name = "isExpire", description = "Whether the attachment capacity has expired."
            + " By default, it has not expired", schema = @Schema(type = "boolean"), in =
            ParameterIn.QUERY, example = "true"),
        @Parameter(name = PAGE_PARAM, description = "paging parameter", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    public ResponseData<PageInfo<SpaceCapacityPageVO>> getCapacityDetail(
        @RequestParam(name = "isExpire", defaultValue = "false") Boolean isExpire,
        @PageObjectParam Page page) {
        String spaceId = LoginContext.me().getSpaceId();
        return ResponseData.success(PageHelper.build(
            iBillingCapacityService.getSpaceCapacityDetail(spaceId, isExpire, page)));
    }
}
