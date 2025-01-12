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
 * Single Global Widget Ro.
 */
@Data
@Schema(description = "Single widget release RO")
public class SingleGlobalWidgetRo {

    @NotBlank
    @Schema(description = "the node id", hidden = true)
    private String nodeId;

    @NotBlank
    @Schema(description = "the view id", hidden = true)
    private String viewId;

    @NotBlank
    @Schema(description = "the widget id")
    private String packageId;

    @Schema(description = "Whether to take effect")
    private Boolean isEnabled;

    @Schema(description = "Whether the template")
    private Boolean isTemplate;

    @Schema(description = "template component source address")
    private String openSourceAddress;

    @Schema(description = "template widget extension cover")
    private String templateCover;

    @Schema(description = "official widget website")
    private String website;

    @NotBlank
    @Schema(description = "record id", hidden = true)
    private String recordId;

}
