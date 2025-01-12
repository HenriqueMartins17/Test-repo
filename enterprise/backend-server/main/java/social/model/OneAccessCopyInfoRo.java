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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Sync organization and user information for OneAccess.
 */
@Data
@Schema(description = "Sync organization and user information for OneAccess")
public class OneAccessCopyInfoRo {

    @Schema(description = "Link-Id")
    @NotBlank(message = "Share link Id is not allowed to be empty")
    private String linkId;

    @Schema(description = "List of people IDs to be synchronized")
    @NotNull(message = "members Field required")
    private List<MemberRo> members;

    @Schema(description = "List of group IDs to be synchronized")
    @NotNull(message = "teamIds Field required")
    private List<String> teamIds;

    /**
     * MemberRo.
     */
    @Data
    public static class MemberRo {

        // member Id
        private String memberId;

        private String unitId;

        private String teamId = "";
    }

}
