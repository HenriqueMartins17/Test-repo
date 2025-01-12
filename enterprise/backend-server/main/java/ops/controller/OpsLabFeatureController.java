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

import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;
import static com.apitable.space.enums.LabsApplicantTypeEnum.SPACE_LEVEL_FEATURE;
import static com.apitable.space.enums.LabsApplicantTypeEnum.USER_LEVEL_FEATURE;
import static com.apitable.space.enums.LabsFeatureEnum.UNKNOWN_LAB_FEATURE;
import static com.apitable.space.enums.LabsFeatureEnum.ofLabsFeature;
import static com.apitable.space.enums.LabsFeatureException.FEATURE_KEY_IS_NOT_EXIST;
import static com.apitable.space.enums.LabsFeatureException.FEATURE_SCOPE_IS_NOT_EXIST;
import static com.apitable.space.enums.LabsFeatureException.FEATURE_TYPE_IS_NOT_EXIST;
import static com.apitable.space.enums.LabsFeatureScopeEnum.UNKNOWN_SCOPE;
import static com.apitable.space.enums.LabsFeatureScopeEnum.ofLabsFeatureScope;
import static com.apitable.space.enums.LabsFeatureTypeEnum.UNKNOWN_LABS_FEATURE_TYPE;
import static com.apitable.space.enums.LabsFeatureTypeEnum.ofLabsFeatureType;
import static com.apitable.space.enums.SpaceException.SPACE_NOT_EXIST;
import static com.apitable.user.enums.UserException.USER_NOT_EXIST;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.ops.ro.LabFeatureApplyRo;
import com.apitable.enterprise.ops.ro.LabFeatureCreateRo;
import com.apitable.enterprise.ops.ro.LabFeatureEditRo;
import com.apitable.enterprise.ops.ro.OpsAuthRo;
import com.apitable.enterprise.ops.service.IOpsLabFeatureService;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.space.entity.LabsApplicantEntity;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.enums.LabsFeatureEnum;
import com.apitable.space.enums.LabsFeatureScopeEnum;
import com.apitable.space.enums.LabsFeatureTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Lab Feature API.
 * </p>
 */
@Slf4j
@RestController
@ApiResource(path = "/ops/lab/features")
@Tag(name = "Product Operation System - Lab Feature API")
public class OpsLabFeatureController {

    @Resource
    private IGmService iGmService;

    @Resource
    private IOpsService iOpsService;

    @Resource
    private IOpsLabFeatureService iOpsLabFeatureService;

    /**
     * Create laboratory feature.
     */
    @PostResource(path = "", requiredPermission = false)
    @Operation(summary = "Create Laboratory Feature")
    public ResponseData<Void> create(@RequestBody @Valid LabFeatureCreateRo body) {
        log.info("The operator「{}」 create laboratory feature「{}」",
            SessionContext.getUserId(), body.getKey());
        iOpsService.auth(body.getToken());
        // Verify the laboratory feature unique identifier.
        LabsFeatureEnum feature = ofLabsFeature(body.getKey());
        ExceptionUtil.isFalse(Objects.equals(feature, UNKNOWN_LAB_FEATURE),
            FEATURE_KEY_IS_NOT_EXIST);
        // Verify the laboratory feature scope.
        LabsFeatureScopeEnum scope = ofLabsFeatureScope(body.getScope());
        ExceptionUtil.isFalse(Objects.equals(scope, UNKNOWN_SCOPE),
            FEATURE_SCOPE_IS_NOT_EXIST);
        // Verify the laboratory feature type.
        LabsFeatureTypeEnum type = ofLabsFeatureType(body.getType());
        ExceptionUtil.isFalse(Objects.equals(type, UNKNOWN_LABS_FEATURE_TYPE),
            FEATURE_TYPE_IS_NOT_EXIST);
        iOpsLabFeatureService.create(feature, scope, type, body.getUrl());
        return ResponseData.success();
    }

    /**
     * Modify laboratory feature.
     */
    @PostResource(path = "/{featureKey}", method = RequestMethod.PATCH,
        requiredPermission = false)
    @Operation(summary = "Modify Laboratory Feature")
    @Parameter(name = "featureKey", description = "Feature Key", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "robot")
    public ResponseData<Void> edit(
        @PathVariable("featureKey") String featureKey,
        @RequestBody LabFeatureEditRo body
    ) {
        log.info("The operator「{}」 modify laboratory feature「{}」",
            SessionContext.getUserId(), featureKey);
        iOpsService.auth(body.getToken());
        iOpsLabFeatureService.edit(featureKey, body.getScope(), body.getType(), body.getUrl());
        return ResponseData.success();
    }

    /**
     * Delete laboratory feature.
     */
    @PostResource(path = "/{featureKey}", method = RequestMethod.DELETE,
        requiredPermission = false)
    @Operation(summary = "Delete Laboratory Feature")
    @Parameter(name = "featureKey", description = "Feature Key", required = true,
        schema = @Schema(type = "string"), in = ParameterIn.PATH,
        example = "render_prompt|async_compute|robot|widget_center")
    public ResponseData<Void> delete(
        @PathVariable("featureKey") String featureKey,
        @RequestBody OpsAuthRo body
    ) {
        log.info("The operator「{}」 delete laboratory feature「{}」",
            SessionContext.getUserId(), featureKey);
        iOpsService.auth(body.getToken());
        iOpsLabFeatureService.remove(featureKey);
        return ResponseData.success();
    }

    /**
     * Open laboratory feature for applicants.
     */
    @PostResource(path = "/{featureKey}/apply", requiredPermission = false)
    @Operation(summary = "Open laboratory feature for applicants")
    public ResponseData<Void> applyLabsFeature(
        @PathVariable("featureKey") String featureKey,
        @RequestBody @Valid LabFeatureApplyRo body
    ) {
        iOpsService.auth(body.getToken());
        iOpsLabFeatureService.apply(featureKey, body.getApplyUserId(), body.getSpaceId());
        return ResponseData.success();
    }

}
