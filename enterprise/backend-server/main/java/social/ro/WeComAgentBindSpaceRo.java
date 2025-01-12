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

package com.apitable.enterprise.social.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * WeCom application tenants bind space station request parameters.
 * </p>
 */
@Data
@Schema(description = "We Com application tenants bind space station request parameters")
public class WeComAgentBindSpaceRo {

    @NotBlank
    @Schema(description = "Space identification", example = "spc2123hjhasd")
    private String spaceId;

    @NotBlank
    @Schema(description = "The code parameter returned by redirection after the user allows "
        + "authorization", example = "CODE")
    private String code;

}
