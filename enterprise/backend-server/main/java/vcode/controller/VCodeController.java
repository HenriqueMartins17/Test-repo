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

package com.apitable.enterprise.vcode.controller;

import static com.apitable.shared.constants.PageConstants.PAGE_DESC;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.vcode.ro.VCodeCreateRo;
import com.apitable.enterprise.vcode.ro.VCodeUpdateRo;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vcode.vo.VCodePageVo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * VCode System - VCode API.
 * </p>
 */
@RestController
@Tag(name = "VCode System - VCode API")
@ApiResource(path = "/vcode")
public class VCodeController {

    @Resource
    private IVCodeService iVCodeService;

    @Resource
    private IGmService iGmService;

    /**
     * Query VCode Page.
     */
    @GetResource(path = "/page", requiredPermission = false)
    @Operation(summary = "Query VCode Page", description = PAGE_DESC)
    @Parameters({
        @Parameter(name = "type", description = "Type (0: official invitation code; 2: redemption"
            + " code)", schema = @Schema(type = "integer"), in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "activityId", description = "Activity ID", schema = @Schema(type =
            "string"), in = ParameterIn.QUERY, example = "1296402001573097473"),
        @Parameter(name = PAGE_PARAM, description = "Page Params", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<VCodePageVo>> page(
        @RequestParam(name = "type", required = false) Integer type,
        @RequestParam(name = "activityId", required = false) Long activityId,
        @PageObjectParam Page page) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_QUERY);
        return ResponseData.success(
            PageHelper.build(iVCodeService.getVCodePageVo(page, type, activityId)));
    }

    /**
     * Create VCode.
     */
    @PostResource(path = "/create", requiredPermission = false)
    @Operation(summary = "Create VCode")
    public ResponseData<List<String>> create(@RequestBody @Valid VCodeCreateRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_MANAGE);
        List<String> codes = iVCodeService.create(userId, ro);
        return ResponseData.success(codes);
    }

    /**
     * Edit VCode Setting.
     */
    @PostResource(path = "/edit/{code}", requiredPermission = false)
    @Operation(summary = "Edit VCode Setting")
    @Parameter(name = "code", description = "VCode", required = true, schema = @Schema(type =
        "string"), in = ParameterIn.PATH, example = "vc123")
    public ResponseData<Void> edit(@PathVariable("code") String code,
                                   @RequestBody @Valid VCodeUpdateRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_MANAGE);
        iVCodeService.edit(userId, code, ro);
        return ResponseData.success();
    }

    /**
     * Delete VCode.
     */
    @PostResource(path = "/delete/{code}", method = {RequestMethod.DELETE,
        RequestMethod.POST}, requiredPermission = false)
    @Operation(summary = "Delete VCode")
    @Parameter(name = "code", description = "VCode", required = true, schema = @Schema(type =
        "string"), in = ParameterIn.PATH, example = "vc123")
    public ResponseData<Void> delete(@PathVariable("code") String code) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_MANAGE);
        // Update delete status
        iVCodeService.delete(userId, code);
        return ResponseData.success();
    }

    /**
     * Exchange VCode.
     */
    @PostResource(path = "/exchange/{code}", requiredPermission = false)
    @Operation(summary = "Exchange VCode")
    @Parameter(name = "code", description = "VCode", required = true, schema = @Schema(type =
        "string"), in = ParameterIn.PATH, example = "vc123")
    public ResponseData<Integer> exchange(@PathVariable("code") String code) {
        Long userId = SessionContext.getUserId();
        // Check redemption code
        iVCodeService.checkRedemptionCode(userId, code.toLowerCase());
        // Use redemption code
        Integer integer = iVCodeService.useRedemptionCode(userId, code.toLowerCase());
        return ResponseData.success(integer);
    }

}
