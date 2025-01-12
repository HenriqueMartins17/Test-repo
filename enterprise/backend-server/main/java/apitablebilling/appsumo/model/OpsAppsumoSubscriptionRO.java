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

package com.apitable.enterprise.apitablebilling.appsumo.model;

import com.apitable.enterprise.ops.ro.OpsAuthRo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * manu execute appsumo subscription parameters.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "manu execute appsumo subscription")
public class OpsAppsumoSubscriptionRO extends OpsAuthRo {

    @Schema(description = "appsumo event id", example = "1244")
    private Long eventId;

    @Schema(description = "space id", example = "spc**")
    private String spaceId;
}
