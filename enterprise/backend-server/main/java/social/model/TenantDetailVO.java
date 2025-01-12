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

import cn.hutool.core.date.DatePattern;
import com.apitable.shared.support.serializer.ImageSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Tenant information.
 */
@Data
@Schema(description = "Tenant information")
public class TenantDetailVO {

    @Schema(description = "List of bound spaces")
    private List<Space> spaces;

    @Schema(description = "Enterprise ID", example = "17236123")
    private String tenantKey;

    @Schema(description = "Enterprise name", example = "Enterprise name")
    private String tenantName;

    @Schema(description = "Corporate avatar", example = "https://....")
    private String avatar;

    @Setter
    @Getter
    public static class Space {

        @Schema(description = "Space identification", example = "spc21182sjahsd")
        private String spaceId;

        @Schema(description = "Space name", example = "vika")
        private String spaceName;

        @Schema(description = "Space logo", example = "logo")
        @JsonSerialize(using = ImageSerializer.class)
        private String spaceLogo;

        @Schema(description = "Primary administrator ID", example = "123")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long mainAdminUserId;

        @Schema(description = "Primary administrator name", example = "LiSi")
        private String mainAdminUserName;

        @Schema(description = "Head portrait of the main administrator", example = "logo")
        @JsonSerialize(using = ImageSerializer.class)
        private String mainAdminUserAvatar;

        @Schema(description = "Subscription product name", example = "Bronze")
        private String product;

        @Schema(description = "Subscription expiration time, blank if free", example = "2019-01-01")
        @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deadline;
    }
}
