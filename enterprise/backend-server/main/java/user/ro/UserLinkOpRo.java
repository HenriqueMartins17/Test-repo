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

package com.apitable.enterprise.user.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * Account Association Request Parameters.
 * </p>
 */
@Data
@Schema(description = "Account Association Request Parameters")
public class UserLinkOpRo {

    @NotNull(message = "Third party type cannot be empty")
    @Schema(description = "Third party type(0.DingTalk;1.WeChat;2.QQ)", example = "1", required =
        true)
    private Integer type;
}
