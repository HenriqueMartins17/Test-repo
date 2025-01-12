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

package com.vikadata.social.service.dingtalk.exception;

import com.vikadata.social.dingtalk.exception.DingTalkExceptionConstants;

/**
 * api current limit exception
 */
public class DingTalkApiLimitException extends RuntimeException {

    private static final long serialVersionUID = 8619308473051715042L;

    private final int code;

    public DingTalkApiLimitException(String msg) {
        this(DingTalkExceptionConstants.UNKNOWN_EXCEPTION_ERR_CODE, msg);
    }

    public DingTalkApiLimitException(int code, String msg) {
        super("code :" + code + ", " + msg);
        this.code = code;
    }

    public DingTalkApiLimitException(int code, String msg, Throwable e) {
        super("code :" + code + ", " + msg, e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
