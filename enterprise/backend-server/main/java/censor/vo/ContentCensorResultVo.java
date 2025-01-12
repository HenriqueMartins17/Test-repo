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

package com.apitable.enterprise.censor.vo;

import com.apitable.shared.support.serializer.ChinaLocalDateTimeToUtcSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Content security - report information vo.
 * </p>
 */
@Data
@Schema(description = "Content security - report information vo")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ContentCensorResultVo {

    @NotBlank(message = "Reported vika ID")
    @Schema(description = "Reported vika ID", example = "dstjuHFsxyvH6751p1")
    private String nodeId;

    @NotBlank(message = "The name of the reported vika table")
    @Schema(description = "The name of the reported vika table", example = "Connotation table")
    private String nodeName;

    @NotBlank(message = "The name of the reported vika table")
    @Schema(description = "The name of the reported vika table", example = "Connotation table")
    private String shareId;

    @NotBlank(message = "Processing result: 0 not processed, 1 banned, 2 normal (unsealed)")
    @Schema(description = "Processing result: 0 not processed, 1 banned, 2 normal (unsealed)",
        example = "1")
    private Integer reportResult;

    @NotBlank(message = "Times of being reported")
    @Schema(description = "Times of being reported", example = "666")
    private int reportNum;

    @Schema(description = "Creation time", example = "2020-03-18T15:29:59.000")
    @JsonSerialize(using = ChinaLocalDateTimeToUtcSerializer.class)
    private LocalDateTime createdAt;

    @Schema(description = "Update time", example = "2020-03-18T15:29:59.000")
    @JsonSerialize(using = ChinaLocalDateTimeToUtcSerializer.class)
    private LocalDateTime updatedAt;

}
