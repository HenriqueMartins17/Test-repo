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
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AutomationTriggerTypeCreateRO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AutomationTriggerTypeCreateRO")
public class AutomationTriggerTypeCreateRO extends OpsAuthRo {

    @Schema(description = "service id")
    @NotBlank
    private String serviceId;

    @Schema(description = "trigger type id")
    private String triggerTypeId;

    @Schema(description = "name")
    @NotBlank
    private String name;

    @Schema(description = "description")
    private String description;

    @Schema(description = "input JSON format")
    private String inputJsonSchema;

    @Schema(description = "output JSON format")
    private String outputJsonSchema;

    @Schema(description = "trigger prototype endpoint")
    @NotBlank
    private String endpoint;

    @Schema(description = "i18n package")
    private String i18n;

}
