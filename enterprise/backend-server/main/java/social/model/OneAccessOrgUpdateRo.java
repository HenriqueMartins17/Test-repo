/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OneAccess Update Organization Ro.
 */
@Data
@Schema(description = "OneAccess Update Organization Ro")
public class OneAccessOrgUpdateRo extends OneAccessBaseRo {

    @Schema(description = "organization id", example = "1011")
    @NotBlank(message = "Organization id is not allowed to be empty")
    private String bimOrgId;

    @Schema(description = "External Organization Id", example = "p-1011")
    @NotBlank(message = "External organization Id is not allowed to be empty")
    private String orgId;

    @Schema(description = "department name", example = "xx-part")
    @NotBlank(message = "department name")
    private String orgName;

    @Schema(description = "Parent organization id", example = "1000")
    private String parentOrgId;

    @Schema(description = "Whether to enable", example = "true")
    private boolean __ENABLE__ = true;

}
