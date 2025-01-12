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

package com.apitable.enterprise.social.model;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OneAccess query organization information.
 */
@Data
@Schema(description = "OneAccess query organization information")
public class OneAccessOrgQueryRo extends OneAccessBaseRo {

    @Schema(description = "orgId", example = "1001")
    private String bimOrgId;

    @Schema(description = "openOrgId", example = "1001")
    private String openOrgId;
}
