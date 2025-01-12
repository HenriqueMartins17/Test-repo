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

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ops.ro.MigrationResourcesRo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.starter.oss.core.OssClientTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Asset API.
 * </p>
 */
@RestController
@ApiResource(path = "/ops/asset")
@Tag(name = "Product Operation System - Asset API")
public class OpsAssetController {
    @Autowired(required = false)
    private OssClientTemplate ossTemplate;

    @Value("${ENABLE_MIGRATION_RESOURCES_TOKEN:K9vvkTLy2eaViE4BAjpuCHEn}")
    private String enableMigrationResourcesToken;

    @PostResource(path = "/migration", requiredLogin = false)
    @Operation(summary = "migration resources")
    public ResponseData<Void> migrationResources(@RequestBody MigrationResourcesRo data) {
        if (!enableMigrationResourcesToken.equalsIgnoreCase(data.getToken())) {
            return ResponseData.error("Token error.");
        }
        List<String> resourceKeys = data.getResourceKeys();
        for (String resourceKey : resourceKeys){
            ossTemplate.migrationResources(data.getSourceBucket(),
                data.getTargetBucket(), resourceKey);
        }
        return ResponseData.success();
    }
}
