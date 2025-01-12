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

package com.apitable.enterprise.vcode.ro;

import com.apitable.core.support.deserializer.StringToLongDeserializer;
import com.apitable.shared.support.deserializer.DateFormatToLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * V code creation request parameters.
 * </p>
 */
@Data
@Schema(description = "V code creation request parameters")
public class VCodeCreateRo {

    @Schema(description = "Create quantity", type = "java.lang.Integer", example = "1", required
        = true)
    @NotNull(message = "Quantity cannot be empty")
    @Min(value = 1, message = "Quantity must be greater than or equal to 1")
    private Integer count;

    @Schema(description = "Type of V code (0: official invitation code; 2: exchange code)", type
        = "java.lang.Integer", example = "0", required = true)
    @NotNull(message = "Type cannot be empty")
    private Integer type;

    @Schema(description = "Activity ID", type = "java.lang.String", example =
        "1296402001573097473", required = true)
    @NotNull(message = "Activity ID cannot be empty")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long activityId;

    @Schema(description = "Redemption template ID (required when the type is redemption code)",
        type = "java.lang.String", example = "1296405974262652930")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long templateId;

    @Schema(description = "The total number of times a single V code can be used (- 1 represents "
        + "an unlimited number of times, 1 by default)", type = "java.lang.Integer", example = "-1")
    @Min(value = -1, message = "Total number available setting error")
    private Integer availableTimes = 1;

    @Schema(description = "Single V code can only be used by one person. The default is 1 time",
        type = "java.lang.Integer", example = "1")
    @Min(value = -1, message = "Wrong setting of single person limit")
    private Integer limitTimes = 1;

    @Schema(description = "Expiration time", example = "2020-03-18T15:29:59.000Z/yyyy-MM-dd( "
        + "HH:mm(:ss)(.SSS))")
    @JsonDeserialize(using = DateFormatToLocalDateTimeDeserializer.class)
    private LocalDateTime expireTime;

    @Schema(description = "Specify the mobile phone number of the user's account", example =
        "12580")
    private String mobile;

}
