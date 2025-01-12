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

package com.apitable.enterprise.censor.controller;

import static com.apitable.shared.constants.PageConstants.PAGE_DESC;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;

import com.apitable.auth.enums.AuthException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.censor.ro.ContentCensorReportRo;
import com.apitable.enterprise.censor.service.IContentCensorResultService;
import com.apitable.enterprise.censor.vo.ContentCensorResultVo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Content Risk Control API.
 */
@RestController
@Tag(name = "Content Risk Control API")
@ApiResource(path = "/censor")
public class CensorController {

    @Resource
    private IContentCensorResultService censorResultService;

    @GetResource(path = "/reports/page", requiredLogin = false)
    @Operation(summary = "Paging query report information list", description =
        "Paging query report information list, each table corresponds to a row of records, and "
            + "the number of reports is automatically accumulated"
            + PAGE_DESC)
    @Parameters({
        @Parameter(name = "status", description = "Processing result, 0 unprocessed, 1 banned, 2 "
            + "normal (unblocked)", required = true, schema = @Schema(type = "integer"), in =
            ParameterIn.QUERY, example = "1"),
        @Parameter(name = PAGE_PARAM, description = "Paging parameters, see the interface "
            + "description for instructions", required = true, schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    public ResponseData<PageInfo<ContentCensorResultVo>> readReports(
        @RequestParam(name = "status", defaultValue = "0") Integer status,
        @PageObjectParam Page page) {
        IPage<ContentCensorResultVo> pageResult = censorResultService.readReports(status, page);
        return ResponseData.success(PageHelper.build(pageResult));
    }

    /**
     * Submit a report.
     */
    @PostResource(path = "/createReports", requiredLogin = false)
    @Operation(summary = "Submit a report")
    public ResponseData<Void> createReports(@RequestBody ContentCensorReportRo censorReportRo) {
        // If it is an anonymous user, verify the cookie to prevent malicious submission
        censorResultService.createReports(censorReportRo);
        return ResponseData.success();
    }

    /**
     * Handling whistleblower information.
     */
    @PostResource(path = "/updateReports", requiredLogin = false)
    @Operation(summary = "Handling whistleblower information", description = "Force to open in "
        + "DingTalk, automatically acquire DingTalk users")
    @Parameters({
        @Parameter(name = "nodeId", description = "node id", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "dstPv5DSHqXknU6Skp"),
        @Parameter(name = "status", description = "Processing result, 0 unprocessed, 1 banned, 2 "
            + "normal (unblocked)", required = true, schema = @Schema(type = "integer"), in =
            ParameterIn.QUERY, example = "1")
    })
    public ResponseData<Void> updateReports(@RequestParam("nodeId") String nodeId,
                                            @RequestParam("status") Integer status) {
        // Query the DingTalk member information in the session
        String auditorUserId = SessionContext.getDingtalkUserId();
        ExceptionUtil.isNotNull(auditorUserId, AuthException.UNAUTHORIZED);
        censorResultService.updateReports(nodeId, status);
        return ResponseData.success();
    }
}
