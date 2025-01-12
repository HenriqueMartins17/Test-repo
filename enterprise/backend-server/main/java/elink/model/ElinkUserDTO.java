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

package com.apitable.enterprise.elink.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Elink User DTO
 *
 * @author penglong feng
 */
@Data
@Schema(description = "Elink user information")
public class ElinkUserDTO {

    /**
     * Open Id.
     */
    @NotBlank(message = "OpenId can't be blank")
    private String openId;

    /**
     * user name
     */
    @NotBlank(message = "userName is not allowed to be empty")
    private String userName;

    /**
     * phone number
     */
    private String mobile;

    /**
     * email
     */
    private String email;

    /**
     * gender
     */
    private int gender;

    /**
     * jobNumber
     */
    private String jobNumber;

    /**
     * status
     */
    private int status = 1;

    /**
     * unitIds
     */
    private List<String> unitIds;

}
