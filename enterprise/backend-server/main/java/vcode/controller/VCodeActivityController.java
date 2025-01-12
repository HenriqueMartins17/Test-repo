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
import com.apitable.enterprise.vcode.ro.VCodeActivityRo;
import com.apitable.enterprise.vcode.service.IVCodeActivityService;
import com.apitable.enterprise.vcode.vo.VCodeActivityPageVo;
import com.apitable.enterprise.vcode.vo.VCodeActivityVo;
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
 * VCode System - Activity API.
 * </p>
 */
@RestController
@Tag(name = "VCode - Activity API")
@ApiResource(path = "/vcode/activity")
public class VCodeActivityController {

    @Resource
    private IGmService iGmService;

    @Resource
    private IVCodeActivityService iVCodeActivityService;

    /**
     * Query Activity List.
     */
    @GetResource(path = "/list", requiredPermission = false)
    @Operation(summary = "Query Activity List")
    @Parameter(name = "keyword", description = "Keyword", schema = @Schema(type = "string"), in =
        ParameterIn.QUERY, example = "channel")
    public ResponseData<List<VCodeActivityVo>> list(
        @RequestParam(name = "keyword", required = false) String keyword) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.ACTIVITY_QUERY);
        return ResponseData.success(iVCodeActivityService.getVCodeActivityVo(keyword));
    }

    /**
     * Query Activity Page.
     */
    @GetResource(path = "/page", requiredPermission = false)
    @Operation(summary = "Query Activity Page", description = PAGE_DESC)
    @Parameters({
        @Parameter(name = "keyword", description = "Keyword", schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = "channel"),
        @Parameter(name = PAGE_PARAM, description = "Page params", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<VCodeActivityPageVo>> page(
        @RequestParam(name = "keyword", required = false) String keyword,
        @PageObjectParam Page page) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.ACTIVITY_QUERY);
        return ResponseData.success(
            PageHelper.build(iVCodeActivityService.getVCodeActivityPageVo(page, keyword)));
    }

    /**
     * Create Activity.
     */
    @PostResource(path = "/create", requiredPermission = false)
    @Operation(summary = "Create Activity")
    public ResponseData<VCodeActivityVo> create(@RequestBody @Valid VCodeActivityRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.ACTIVITY_MANAGE);
        Long id = iVCodeActivityService.create(ro);
        return ResponseData.success(
            VCodeActivityVo.builder().activityId(id).name(ro.getName()).scene(ro.getScene())
                .build());
    }

    /**
     * Edit Activity Info.
     */
    @PostResource(path = "/edit/{activityId}", requiredPermission = false)
    @Operation(summary = "Edit Activity Info")
    @Parameter(name = "activityId", description = "Activity ID", required = true, schema =
    @Schema(type = "string"), in = ParameterIn.PATH, example = "12369")
    public ResponseData<Void> edit(@PathVariable("activityId") Long activityId,
                                   @RequestBody VCodeActivityRo ro) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.ACTIVITY_MANAGE);
        iVCodeActivityService.edit(userId, activityId, ro);
        return ResponseData.success();
    }

    /**
     * Delete Activity.
     */
    @PostResource(path = "/delete/{activityId}", method = {RequestMethod.DELETE,
        RequestMethod.POST}, requiredPermission = false)
    @Operation(summary = "Delete Activity")
    @Parameter(name = "activityId", description = "Activity ID", required = true, schema =
    @Schema(type = "string"), in = ParameterIn.PATH, example = "12369")
    public ResponseData<Void> delete(@PathVariable("activityId") Long activityId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.ACTIVITY_MANAGE);
        // Check if activity exists
        iVCodeActivityService.checkActivityIfExist(activityId);
        // Update delete status
        iVCodeActivityService.delete(userId, activityId);
        return ResponseData.success();
    }
}
