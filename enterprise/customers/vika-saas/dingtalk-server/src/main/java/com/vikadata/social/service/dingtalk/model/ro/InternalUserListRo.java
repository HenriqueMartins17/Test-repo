/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.social.service.dingtalk.model.ro;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Internal interface call--get the parameters of the detailed list of users in the DingTalk department
 */
@Data
@ApiModel(value = "Internal interface call--get the parameters of the detailed list of users in the DingTalk department")
public class InternalUserListRo {
    @ApiModelProperty(value = "suiteId", dataType = "java.lang.String", example = "12345", required = true)
    private String suiteId;

    @ApiModelProperty(value = "authCorpId", dataType = "java.lang.String", example = "corpdfkdaj", required = true)
    private String authCorpId;

    @ApiModelProperty(value = "deptId", dataType = "java.lang.String", example = "1234L", required = true)
    private Long deptId;

    @ApiModelProperty(value = "cursor", dataType = "java.lang.Integer", example = "0")
    private Integer cursor = 0;

    @ApiModelProperty(value = "size", dataType = "java.lang.Integer", example = "100")
    private Integer size = 100;
}
