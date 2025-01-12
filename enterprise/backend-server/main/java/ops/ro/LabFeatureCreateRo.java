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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * LabFeatureCreateRo.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LabFeatureCreateRo")
public class LabFeatureCreateRo extends OpsAuthRo {

    @Schema(description = "Experimental functional level",
        type = "java.lang.String", example = "user|space")
    private String scope;

    @Schema(description = "Unique identification of laboratory function",
        type = "java.lang.String",
        example = "render_prompt|async_compute|robot|widget_center")
    private String key;

    @Schema(description = "Types of laboratory functions on shelves",
        type = "java.lang.String", example = "static|review|normal")
    private String type;

    @Schema(description = "Lab Function Magic Form Address",
        type = "java.lang.String")
    private String url;
}
