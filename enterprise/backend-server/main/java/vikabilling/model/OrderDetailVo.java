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

package com.apitable.enterprise.vikabilling.model;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * Order Detail View.
 * </p>
 */
@Data
@Schema(description = "Order Detail View")
public class OrderDetailVo {

    @Schema(description = "order no", example = "20220215185035483353")
    private String orderNo;

    @Schema(description = "original price (unit: yuan)", example = "19998.21")
    private BigDecimal priceOrigin;

    @Schema(description = "payment amount (unit: yuan)", example = "18998.11")
    private BigDecimal pricePaid;

    @Schema(description = "pay status", type = "java.lang.String", example = "Canceled")
    private String status;

    @Schema(description = "pay channel type", type = "java.lang.String", example = "wx_pub_qr")
    private String payChannel;

    @Schema(description = "created time", type = "java.lang.String", example = "2022-02-15 "
        + "10:25:20")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdTime;

    @Schema(description = "paid time", type = "java.lang.String", example = "2022-02-15 10:29:20")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime paidTime;

    @Schema(description = "finish time", type = "java.lang.String", example = "2022-02-15 10:29:20")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime finishTime;
}
