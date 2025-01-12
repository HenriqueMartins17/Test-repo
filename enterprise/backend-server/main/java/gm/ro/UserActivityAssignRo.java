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

import com.apitable.core.support.deserializer.StringArrayToLongArrayDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * User Activity Assign Ro.
 */
@Data
@Schema(description = "User Activity Assign Ro")
public class UserActivityAssignRo {

    @Schema(description = "wizard id", example = "7")
    private Integer wizardId;

    @Schema(description = "specifies the value of the wizard id", example = "7")
    private Integer value;

    @Schema(description = "specifying user id list（choose one of the two phone numbers with the "
        + "test）", example = "[\"10101\",\"10102\",\"10103\",\"10104\"]")
    @JsonDeserialize(using = StringArrayToLongArrayDeserializer.class)
    private List<Long> userIds;

    @Schema(description = "mobile phone number of the test account", example = "1340000")
    private String testMobile;
}
