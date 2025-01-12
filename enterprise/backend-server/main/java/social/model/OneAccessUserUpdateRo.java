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
 * OneAccess User update ro.
 */
@Data
@Schema(description = "OneAccess User update ro")
public class OneAccessUserUpdateRo extends OneAccessBaseRo {

    @Schema(description = "bimUid")
    @NotBlank(message = "bimUid can not be empty")
    private String bimUid;

    @Schema(description = "Account Field Properties")
    @NotBlank(message = "Account cannot be empty")
    private String loginName;

    @Schema(description = "mobile number", example = "13800000000")
    @NotBlank(message = "mobile number can not be blank")
    private String mobile;

    @Schema(description = "E-mail account", example = "test001@vikatest.com")
    private String email;

    @Schema(description = "OrganizationId", example = "1011")
    private String orgId;

    @Schema(description = "Account full name (name)", example = "bob")
    private String fullName;
}
