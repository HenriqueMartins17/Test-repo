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

package com.apitable.enterprise.censor.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Content security - report information ro.
 * </p>
 */
@Data
@Schema(description = "Content security - report information ro")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ContentCensorReportRo {

    @NotBlank(message = "Reported vika")
    @Schema(description = "Reported vika", example = "dstjuHFsxyvH6751p1")
    private String nodeId;

    @NotBlank(message = "Reasons for reporting")
    @Schema(description = "Reasons for reporting", example = "Pornographic and vulgar")
    private String reportReason;


}
