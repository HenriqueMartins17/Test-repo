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

package com.vikadata.scheduler.space.cache;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

public class RedisKey {

    public static final String GENERAL_STATICS = "vikadata:statics:{}:{}";

    private static final String USER_ACTIVE_SPACE_KEY = "vikadata:cache:user:{}:space:active";

    /**
     * Get user active space
     *
     * @param userId user's id
     * @return user information storage key
     */
    public static String getUserActiveSpaceKey(Long userId) {
        Assert.notNull(userId, "user does not exist");
        return StrUtil.format(USER_ACTIVE_SPACE_KEY, userId);
    }
}
