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

package com.apitable.enterprise.integral.controller;

import static com.apitable.shared.constants.PageConstants.PAGE_COMPLEX_EXAMPLE;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.apitable.user.ro.InviteCodeRewardRo;
import com.apitable.user.vo.IntegralRecordVO;
import com.apitable.user.vo.UserIntegralVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integral Api.
 */
@Slf4j
@RestController
@Tag(name = "Integral Api")
@ApiResource
public class IntegralController {

    @Resource
    private IVCodeService ivCodeService;

    @Resource
    private IIntegralService iIntegralService;

    /**
     * Query account integral information.
     */
    @GetResource(path = "/user/integral", requiredPermission = false)
    @Operation(summary = "Query account integral information")
    public ResponseData<UserIntegralVo> integrals() {
        Long userId = SessionContext.getUserId();
        int totalIntegral = iIntegralService.getTotalIntegralValueByUserId(userId);
        UserIntegralVo vo = new UserIntegralVo();
        vo.setTotalIntegral(totalIntegral);
        return ResponseData.success(vo);
    }

    /**
     * Page by page query of integral revenue and expenditure details.
     */
    @GetResource(path = "/user/integral/records", requiredPermission = false)
    @Operation(summary = "Page by page query of integral revenue and expenditure details")
    @Parameter(name = PAGE_PARAM, description = "Page parameter", required = true, schema =
    @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_COMPLEX_EXAMPLE)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<IntegralRecordVO>> integralRecords(@PageObjectParam Page page) {
        Long userId = SessionContext.getUserId();
        IPage<IntegralRecordVO> results =
            iIntegralService.getIntegralRecordPageByUserId(page, userId);
        return ResponseData.success(PageHelper.build(results));
    }

    /**
     * Fill in invitation code reward.
     */
    @PostResource(path = "/user/invite/reward", requiredPermission = false)
    @Operation(summary = "Fill in invitation code reward", description = "Users fill in the "
        + "invitation code and get rewards")
    public ResponseData<Void> inviteCodeReward(@RequestBody @Validated InviteCodeRewardRo body) {
        // Verify the validity of the invitation code
        ivCodeService.checkInviteCode(body.getInviteCode());
        // Fill in the invitation code and reward integral
        Long userId = SessionContext.getUserId();
        iIntegralService.useInviteCodeReward(userId, body.getInviteCode());
        return ResponseData.success();
    }
}
