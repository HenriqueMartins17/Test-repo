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

package com.apitable.enterprise.ops.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * LabFeatureApplyRo.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LabFeatureApplyRo")
public class LabFeatureApplyRo extends OpsAuthRo {

    @Schema(description = "The space station ID of the experimental function"
        + " to be enabled, optional, allowed to be empty",
        type = "java.lang.String", example = "spchhRu3xQqt9")
    private String spaceId;

    @NotBlank(message = "Applicant user cannot be blank")
    @Schema(description = "ID of the user who applies for opening the experimental function",
        type = "java.lang.String", requiredMode = Schema.RequiredMode.REQUIRED,
        example = "a83ec20f15c9459893d133c2c369eff6")
    private String applyUserId;
}
