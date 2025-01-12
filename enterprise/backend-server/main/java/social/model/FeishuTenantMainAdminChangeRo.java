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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * Request parameters of space replacement master administrator.
 * </p>
 */
@Data
@Schema(description = "Lark tenant space change master administrator request parameters")
public class FeishuTenantMainAdminChangeRo {

    @Schema(description = "Tenant ID", example = "128371293xja", required = true)
    @NotBlank(message = "Tenant ID")
    private String tenantKey;

    @Schema(description = "Space identification", example = "spc2123hjhasd", required = true)
    @NotBlank(message = "Space ID cannot be empty")
    private String spaceId;

    @Schema(description = "Member ID of the new master administrator", example = "123456",
        required = true)
    @NotNull(message = "The member ID of the new master administrator cannot be empty")
    private Long memberId;
}
