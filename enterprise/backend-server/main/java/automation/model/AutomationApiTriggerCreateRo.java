/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.automation.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "AutomationApiTrigger")
public class AutomationApiTriggerCreateRo {

    @Schema(description = "robot base info")
    @NotNull(message = "robot not null")
    private AutomationApiRobotRo robot;

    @Schema(description = "trigger input data")
    @NotNull(message = "trigger not null")
    private AutomationApiTriggerRo trigger;

    @Schema(description = "the webhookURL of execute action", example = "https://xxxxx")
    private String webhookUrl;

    @Schema(description = "request sequence, normal 32 uuid", example = "1e16c603908743a8aaa5933faec91973")
    @Length(max = 64)
    private String seqId;

}
