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

package com.apitable.enterprise.gm.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Space Certification Ro.
 */
@Data
@Schema(description = "Space Certification Ro")
public class SpaceCertificationRo {

    @NotBlank(message = "the space id cannot be empty")
    @Schema(description = "space id", required = true, example = "spcNrqN2iH0qK")
    private String spaceId;

    @NotBlank(message = "user uuid")
    @Schema(description = "user uuid", required = true, example = "dfada")
    private String uuid;

    @NotBlank(message = "authentication level")
    @Schema(description = "authentication level", required = true, example = "basic/senior")
    private String certification;
}
