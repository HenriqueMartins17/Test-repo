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
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Space Whitelist Ro.
 */
@Data
@Schema(description = "Space Whitelist Ro")
public class SpaceWhitelistRo {

    @Schema(description = "the space id list", required = true, example = "[\"spczJrh2i3tLW\","
        + "\"spczdmQDfBAn5\"]")
    @NotEmpty(message = "the space id cannot be empty")
    private List<String> spaceIds;

    @Schema(description = "the number of members", example = "100")
    private Integer memberCount;

    @Schema(description = "capacity multiple(*1G)", example = "10")
    private Long capacityMultiple;

    @Schema(description = "file node amount", example = "10")
    private Integer fileCount;

    @Schema(description = "sub admin amount", example = "9")
    private Integer subAdminCount;

    @Schema(description = "effective number of days", example = "9")
    private Integer day;
}
