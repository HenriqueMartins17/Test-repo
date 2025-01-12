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

package com.vikadata.scheduler.space.model;

public class ResponseDataDto<T> {

    private Boolean success;

    /**
     * response status code
     */
    private Integer code;

    /**
     * response status code's message
     */
    private String message;

    /**
     * response object
     */
    private T data;

    public ResponseDataDto() {
        this(false, null, null);
    }

    public ResponseDataDto(Boolean success, Integer code, T data) {
        this.success = success;
        this.code = code;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
