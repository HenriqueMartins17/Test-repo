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

package com.apitable.enterprise.audit.controller;

import static com.apitable.shared.constants.DateFormatConstants.TIME_SIMPLE_PATTERN;
import static com.apitable.space.enums.SpacePermissionException.INSUFFICIENT_PERMISSIONS;

import cn.hutool.core.collection.CollUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.audit.model.SpaceAuditPageParamDTO;
import com.apitable.enterprise.audit.model.SpaceAuditPageVO;
import com.apitable.enterprise.audit.service.ISpaceAuditService;
import com.apitable.shared.cache.bean.UserSpaceDto;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Space - Audit Api.
 */
@RestController
@ApiResource(path = "/space")
@Tag(name = "Space - Audit Api")
public class SpaceAuditController {

    @Resource
    private ISpaceAuditService iSpaceAuditService;

    /**
     * Query space audit logs in pages.
     */
    @GetResource(path = "/{spaceId}/audit", requiredPermission = false)
    @Operation(summary = "Query space audit logs in pages")
    @Parameters({
        @Parameter(name = "spaceId", description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "spc8mXUeiXyVo"),
        @Parameter(name = "beginTime", description = "beginTime(format：yyyy-MM-dd HH:mm:ss)",
            schema = @Schema(type = "string", format = "date-time"), in = ParameterIn.QUERY,
            example = "1"),
        @Parameter(name = "endTime", description = "endTime(format：yyyy-MM-dd HH:mm:ss)",
            schema = @Schema(type = "string", format = "date-time"), in = ParameterIn.QUERY,
            example = "1"),
        @Parameter(name = "memberIds", description = "member ids",
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "1,3,5"),
        @Parameter(name = "actions", description = "actions", schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = "create_node,rename_node"),
        @Parameter(name = "keyword", description = "keyword", schema = @Schema(type = "string"),
            in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "pageNo", description = "page no(default 1)",
            schema = @Schema(type = "integer"), in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "pageSize", description = "page size(default 20，max 100)",
            schema = @Schema(type = "integer"), in = ParameterIn.QUERY, example = "20"),
    })
    public ResponseData<PageInfo<SpaceAuditPageVO>> audit(
        @PathVariable("spaceId") String spaceId,
        @RequestParam(name = "beginTime", required = false)
        @DateTimeFormat(pattern = TIME_SIMPLE_PATTERN) LocalDateTime beginTime,
        @RequestParam(name = "endTime", required = false)
        @DateTimeFormat(pattern = TIME_SIMPLE_PATTERN) LocalDateTime endTime,
        @RequestParam(name = "memberIds", required = false) List<Long> memberIds,
        @RequestParam(name = "actions", required = false) List<String> actions,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(value = "pageNo", defaultValue = "1") @Valid @Min(1) Integer pageNo,
        @RequestParam(value = "pageSize", defaultValue = "20")
        @Valid @Min(5) @Max(100) Integer pageSize
    ) {
        // check whether it is cross space
        UserSpaceDto userSpaceDto = LoginContext.me().getUserSpaceDto(spaceId);
        ExceptionUtil.isTrue(userSpaceDto.isMainAdmin(), INSUFFICIENT_PERMISSIONS);
        // get spatial audit paging information
        SpaceAuditPageParamDTO param = SpaceAuditPageParamDTO.builder()
            .beginTime(beginTime)
            .endTime(endTime)
            .memberIds(memberIds)
            .actions(actions)
            .keyword(keyword)
            .pageNo(pageNo)
            .pageSize(pageSize)
            .build();
        return ResponseData.success(iSpaceAuditService.getSpaceAuditPageVO(spaceId, param));
    }
}
