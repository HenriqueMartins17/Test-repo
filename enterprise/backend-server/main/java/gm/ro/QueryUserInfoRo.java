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
import lombok.Data;

/**
 * query user contact information request param.
 */
@Data
@Schema(description = "query user contact information request param")
public class QueryUserInfoRo {

    @Schema(description = "host", required = true, example = "https://apitable")
    private String host;

    @Schema(description = "datasheetId", required = true, example = "dstyLyo90skGTTfPkw")
    private String datasheetId;

    @Schema(description = "viewId", required = true, example = "viwQBpMksyCqy")
    private String viewId;

    @Schema(description = "token", required = true, example = "uskxVwqyXWmpzM3jxCXBcGK")
    private String token;
}