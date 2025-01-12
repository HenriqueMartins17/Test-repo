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
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

/**
 * Template Unpublish Ro.
 */
@Data
@Schema(description = "Template Unpublish Ro")
public class TemplateUnpublishRo {

    @Schema(description = "template category code; invalid when allCategory is true",
        example = "tpcxxx", requiredMode = RequiredMode.NOT_REQUIRED)
    private String categoryCode;

    @Schema(description = "unpublish from all template categories",
        example = "true", requiredMode = RequiredMode.NOT_REQUIRED)
    private Boolean allCategory;
}
