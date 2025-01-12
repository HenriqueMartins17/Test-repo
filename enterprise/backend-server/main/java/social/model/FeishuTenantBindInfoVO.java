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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Lark Enterprise registration invitation code.
 */
@Data
@Schema(description = "Lark Enterprise registration invitation code")
public class FeishuTenantBindInfoVO {

    @Schema(description = "Invitation code", example = "1263123")
    private String inviteCode;

    @Schema(description = "List of bound spaces")
    private List<BindSpaceInfoVO> bindInfoList;

    /**
     * Bind Space Info VO.
     */
    @Setter
    @Getter
    @ToString
    public static class BindSpaceInfoVO {

        @Schema(description = "Space ID", example = "spc12hjasd")
        private String spaceId;

        @Schema(description = "Space name", example = "Space station")
        private String spaceName;
    }
}
