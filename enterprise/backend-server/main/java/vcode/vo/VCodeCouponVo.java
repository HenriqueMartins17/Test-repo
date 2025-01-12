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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * View of V-code coupon template.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "View of V-code coupon template")
public class VCodeCouponVo {

    @Schema(description = "Exchange coupon template ID", type = "java.lang.String", example =
        "1456")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    @Schema(description = "Exchange amount", type = "java.lang.Integer", example = "10")
    private Integer count;

    @Schema(description = "Remarks", type = "java.lang.String", example = "Seed user benefit "
        + "exchange template")
    private String comment;

}
