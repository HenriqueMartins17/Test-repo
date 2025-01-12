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

package com.apitable.enterprise.ops.controller;

import cn.hutool.core.collection.CollUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ops.ro.SpaceBlacklistRo;
import com.apitable.enterprise.ops.ro.OpsAuthRo;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.space.dto.BaseSpaceInfoDTO;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Space API.
 * </p>
 */
@Slf4j
@RestController
@ApiResource(path = "/ops/spaces")
@Tag(name = "Product Operation System - Space API")
public class OpsSpaceController {

    @Resource
    private IOpsService iOpsService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private SpaceMapper spaceMapper;

    /**
     * Ban space.
     */
    @PostResource(path = "/{spaceId}/ban", requiredLogin = false)
    @Operation(summary = "Ban space", description = "limit function.")
    public ResponseData<Void> banSpace(@PathVariable("spaceId") String spaceId,
                                       @RequestBody OpsAuthRo body) {
        log.info("The operator「{}」ban the space「{}」",
            SessionContext.getUserIdWithoutException(), spaceId);
        // Check permissions.
        iOpsService.auth(body.getToken());
        // Query whether the space exist.
        iSpaceService.getBySpaceId(spaceId);
        // Ban space.
        SpaceGlobalFeature feature = SpaceGlobalFeature.builder().ban(true).build();
        iSpaceService.switchSpacePros(1L, spaceId, feature);
        return ResponseData.success();
    }

    /**
     * Set blacklist.
     */
    @PostResource(path = "/operateBlacklist", requiredLogin = false)
    @Operation(summary = "Set blacklist", hidden = true)
    public ResponseData<Void> setBlacklist(@RequestBody SpaceBlacklistRo body) {
        iOpsService.auth(body.getToken());
        List<BaseSpaceInfoDTO> spaceInfos = spaceMapper.selectBaseSpaceInfo(body.getSpaceIds());
        if (CollUtil.isEmpty(spaceInfos)) {
            throw new BusinessException("Space not exist.");
        }
        for (BaseSpaceInfoDTO info : spaceInfos) {
            SpaceGlobalFeature feature =
                SpaceGlobalFeature.builder().blackSpace(body.getStatus()).build();
            iSpaceService.switchSpacePros(1L, info.getSpaceId(), feature);
        }
        return ResponseData.success();
    }

}
