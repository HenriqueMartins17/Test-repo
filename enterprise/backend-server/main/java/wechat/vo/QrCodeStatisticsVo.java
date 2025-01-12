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

package com.apitable.enterprise.wechat.vo;

import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * Statistical data view of QR code.
 * </p>
 */
@Data
@Schema(description = "Statistical data view of QR code")
public class QrCodeStatisticsVo {

    @Schema(description = "Number of visitors", type = "java.lang.Integer", example = "15")
    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Integer viewUserCount;

    @Schema(description = "Total Visits", type = "java.lang.Integer", example = "20")
    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Integer viewCount;
}
