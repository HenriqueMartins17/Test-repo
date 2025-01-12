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

package com.apitable.enterprise.social.model;

import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DingTalk ISV User Login Request Parameters.
 */
@Schema(description = "DingTalk Application Workbench Administrator Login Return Information")
@Data
public class DingTalkIsvAdminUserLoginVo {

    @Schema(description = "Space ID bound by the application")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String bindSpaceId;

    @Schema(description = "Enterprise ID of the third-party authorized organization")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String corpId;
}
