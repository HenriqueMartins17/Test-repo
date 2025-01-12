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

package com.apitable.enterprise.vcode.vo;

import static com.apitable.shared.constants.DateFormatConstants.TIME_SIMPLE_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Page view of V-code coupon template.
 * </p>
 */
@Data
@Schema(description = "Page view of V-code coupon template")
@EqualsAndHashCode(callSuper = true)
public class VCodeCouponPageVo extends VCodeCouponVo {

    @Schema(description = "Creator", type = "java.lang.String", example = "Zhang San")
    private String creator;

    @Schema(description = "Create time", example = "2019-01-01 10:12:13")
    @JsonFormat(pattern = TIME_SIMPLE_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @Schema(description = "Last Modified By", type = "java.lang.String", example = "Li Si")
    private String updater;

    @Schema(description = "Last modified", example = "2019-01-01 10:12:13")
    @JsonFormat(pattern = TIME_SIMPLE_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
}
