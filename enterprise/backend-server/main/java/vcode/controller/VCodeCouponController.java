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
import com.apitable.enterprise.vcode.ro.VCodeCouponRo;
import com.apitable.enterprise.vcode.service.IVCodeCouponService;
import com.apitable.enterprise.vcode.vo.VCodeCouponPageVo;
import com.apitable.enterprise.vcode.vo.VCodeCouponVo;
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
 * VCode System - Coupon API.
 * </p>
 */
@RestController
@Tag(name = "VCode System - Coupon API")
@ApiResource(path = "/vcode/coupon")
public class VCodeCouponController {

    @Resource
    private IVCodeCouponService iVCodeCouponService;

    @Resource
    private IGmService iGmService;

    /**
     * Query Coupon View List.
     */
    @GetResource(path = "/list", requiredPermission = false)
    @Operation(summary = "Query Coupon View List")
    @Parameter(name = "keyword", description = "Keyword", schema = @Schema(type = "string"), in =
        ParameterIn.QUERY, example = "channel")
    public ResponseData<List<VCodeCouponVo>> list(
        @RequestParam(name = "keyword", required = false) String keyword) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_COUPON_QUERY);
        return ResponseData.success(iVCodeCouponService.getVCodeCouponVo(keyword));
    }

    /**
     * Query Coupon Page.
     */
    @GetResource(path = "/page", requiredPermission = false)
    @Operation(summary = "Query Coupon Page", description = PAGE_DESC)
    @Parameters({
        @Parameter(name = "keyword", description = "Keyword", schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = "channel"),
        @Parameter(name = PAGE_PARAM, description = "Page Params", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<VCodeCouponPageVo>> page(
        @RequestParam(name = "keyword", required = false) String keyword,
        @PageObjectParam Page page) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_COUPON_QUERY);
        return ResponseData.success(
            PageHelper.build(iVCodeCouponService.getVCodeCouponPageVo(page, keyword)));
    }

    /**
     * Create Coupon Template.
     */
    @PostResource(path = "/create", requiredPermission = false)
    @Operation(summary = "Create Coupon Template")
    public ResponseData<VCodeCouponVo> create(@RequestBody @Valid VCodeCouponRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_COUPON_MANAGE);
        Long templateId = iVCodeCouponService.create(ro);
        return ResponseData.success(
            VCodeCouponVo.builder().templateId(templateId).count(ro.getCount())
                .comment(ro.getComment()).build());
    }

    /**
     * Edit Coupon Template.
     */
    @PostResource(path = "/edit/{templateId}", requiredPermission = false)
    @Operation(summary = "Edit Coupon Template")
    @Parameter(name = "templateId", description = "Coupon Template ID", required = true, schema =
    @Schema(type = "string"), in = ParameterIn.PATH, example = "12359")
    public ResponseData<Void> edit(@PathVariable("templateId") Long templateId,
                                   @RequestBody VCodeCouponRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_COUPON_MANAGE);
        iVCodeCouponService.edit(userId, templateId, ro);
        return ResponseData.success();
    }

    /**
     * Delete Coupon Template.
     */
    @PostResource(path = "/delete/{templateId}", method = {RequestMethod.DELETE,
        RequestMethod.POST}, requiredPermission = false)
    @Operation(summary = "Delete Coupon Template")
    @Parameter(name = "templateId", description = "Coupon Template ID", required = true, schema =
    @Schema(type = "string"), in = ParameterIn.PATH, example = "12359")
    public ResponseData<Void> delete(@PathVariable("templateId") Long templateId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.V_CODE_COUPON_MANAGE);
        // Verify redemption code
        iVCodeCouponService.checkCouponIfExist(templateId);
        // Update delete status
        iVCodeCouponService.delete(userId, templateId);
        return ResponseData.success();
    }

}
