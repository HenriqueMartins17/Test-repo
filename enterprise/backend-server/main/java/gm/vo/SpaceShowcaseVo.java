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

package com.apitable.enterprise.gm.vo;

import com.apitable.shared.support.serializer.ChinaLocalDateTimeToUtcSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Space Info.
 */
@Data
@Schema(description = "Space Info")
public class SpaceShowcaseVo {

    @Schema(description = "space id", example = "spcxxx", required = true)
    private String spaceId;

    @Schema(description = "space name", example = "space", required = true)
    private String spaceName;

    @Schema(description = "creation time", example = "2020/10/10", required = true)
    @JsonSerialize(using = ChinaLocalDateTimeToUtcSerializer.class)
    private LocalDateTime createdAt;
}
