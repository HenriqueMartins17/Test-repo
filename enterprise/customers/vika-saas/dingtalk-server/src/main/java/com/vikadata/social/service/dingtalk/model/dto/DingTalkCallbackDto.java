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

package com.vikadata.social.service.dingtalk.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * vika api data returned by dingtalk callback
 */
@Data
@ApiModel(value = "vika api data returned")
public class DingTalkCallbackDto {
    @ApiModelProperty(value = "msgSignature", dataType = "java.lang.String", example = "1ojQf0NSvw2WPvW7LijxS8UvISr8pdDP+rXpPbcLGOmIxxxx", required = true)
    @JsonProperty(value = "msg_signature")
    private String msgSignature;

    @ApiModelProperty(value = "nonce", dataType = "java.lang.String", example = "1ojQf0NSvw2WPvW7LijxS8UvISr8pdDP", required = true)
    private String nonce;

    @ApiModelProperty(value = "timeStamp", dataType = "java.lang.String", example = "1ojQf0NSvw2WPvW7LijxS8UvISr8pdDP", required = true)
    private String timeStamp;

    @ApiModelProperty(value = "encrypt", dataType = "java.lang.String", example = "1ojQf0NSvw2WPvW7LijxS8UvISr8pdDP+rXpPbcLGOmIxxxx", required = true)
    private String encrypt;
}
