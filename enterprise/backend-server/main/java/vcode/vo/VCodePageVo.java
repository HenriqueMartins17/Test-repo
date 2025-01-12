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

import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * V code paging view.
 * </p>
 */
@Data
@Schema(description = "V code paging view")
public class VCodePageVo {

    @Schema(description = "Activity Name", type = "java.lang.String", example = "XX Channel "
        + "promotion")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String activityName;

    @Schema(description = "Type of V code (0: official invitation code; 2: exchange code)", type
        = "java.lang.Integer", example = "0")
    private Integer type;

    @Schema(description = "V code", type = "java.lang.String", example = "2Mecwhid")
    private String code;

    @Schema(description = "Remarks on exchange code exchange template", type = "java.lang.String",
        example = "2Mecwhid")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String templateComment;

    @Schema(description = "The total number of times a single V code can be used (- 1 represents "
        + "an unlimited number of times)", type = "java.lang.Integer", example = "-1")
    private Integer availableTimes;

    @Schema(description = "Remaining times", type = "java.lang.Integer", example = "-1")
    private Integer remainTimes;

    @Schema(description = "Single person limited use times (- 1 represents unlimited times)",
        type = "java.lang.Integer", example = "1")
    private Integer limitTimes;

    @Schema(description = "Expiration time", example = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = TIME_SIMPLE_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime expireTime;

    @Schema(description = "Specified user", type = "java.lang.String", example = "A pretty boy")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String assignUser;

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
