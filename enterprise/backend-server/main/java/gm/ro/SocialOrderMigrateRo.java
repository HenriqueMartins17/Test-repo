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

package com.apitable.enterprise.gm.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Social Order Migrate Ro.
 */
@Data
@Schema(description = "Social Order Migrate Ro")
public class SocialOrderMigrateRo {

    @NotBlank(message = "third party board")
    @Schema(description = "1: wecom 2: dingtalk 3: feishu", required = true, example = "2")
    private Integer platformType;


    @Schema(description = "space id ", example = "spc***")
    private String spaceId;
}
