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

package com.vikadata.scheduler.space.enums;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * User Logout Status
 * </p>
 */
@AllArgsConstructor
@Getter
public enum UserLogoutStatus {

    APPLY_LOGOUT(1),

    CANCEL_LOGOUT(2),

    COMPLETE_LOGOUT(3);

    private int statusCode;

    public static UserLogoutStatus ofLogoutStatus(Integer inputLogoutStatus) {
        for (UserLogoutStatus userLogoutStatus : UserLogoutStatus.values()) {
            if (Objects.equals(userLogoutStatus.getStatusCode(), inputLogoutStatus)) {
                return userLogoutStatus;
            }
        }
        return null;
    }
}
