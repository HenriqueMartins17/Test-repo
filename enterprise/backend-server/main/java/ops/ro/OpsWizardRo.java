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
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Wizard configuration request parameters.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Wizard configuration request parameters")
public class OpsWizardRo extends OpsAuthRo {

    @Schema(description = "Configuration content", example = "json")
    private String content;

    @Schema(description = "Rollback or not", example = "true")
    private Boolean rollback;

    @Schema(description = "Language", example = "zh-CN")
    private String lang = "zh_CN";
}
