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

package com.apitable.enterprise.widget.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * Widget audit submit parameter.
 * </p>
 */
@Data
@Schema(description = "Widget audit submit parameter")
public class WidgetAuditSubmitDataRo {

    @NotBlank
    @Schema(description = "Widget Id")
    private String globalPackageId;

    @NotBlank
    @Schema(description = "Submit version")
    private String submitVersion;

    @NotNull
    @Schema(description = "Audit result")
    private Boolean auditResult;

    @Schema(description = "Review remarks")
    private String auditRemark;

    @NotEmpty
    @Schema(description = "datasheet id")
    private String dstId;

    @NotEmpty
    @Schema(description = "field id")
    private String fieldId;

    @NotEmpty
    @Schema(description = "record id")
    private String recordId;

    @Schema(description = "Is Template")
    private Boolean isTemplate;

    @Schema(description = "Is Enabled")
    private Boolean isEnabled;

    @Schema(description = "template component source address")
    private String widgetOpenSource;

    @Schema(description = "template widget extension cover")
    private String templateCover;

}
