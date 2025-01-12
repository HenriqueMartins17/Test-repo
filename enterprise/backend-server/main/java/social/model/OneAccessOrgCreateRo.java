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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OneAccess Create organization ro.
 */
@Data
@Schema(description = "OneAccess Create organization ro")
public class OneAccessOrgCreateRo extends OneAccessBaseRo {

    @Schema(description = "Current Organization Id", example = "100001")
    @NotBlank(message = "organization Id")
    private String orgId;

    @Schema(description = "organization name", example = "xxx-depart")
    @NotBlank(message = "organization name")
    private String orgName;

    @Schema(description = "parent organization id", example = "10001")
    private String parentOrgId;

}
