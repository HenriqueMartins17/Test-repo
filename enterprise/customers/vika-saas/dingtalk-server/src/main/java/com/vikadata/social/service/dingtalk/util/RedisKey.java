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

package com.vikadata.social.service.dingtalk.util;

import cn.hutool.core.util.StrUtil;

public class RedisKey {

    /**
     * DingTalk sync http Distributed lock key
     */
    public static final String DING_TALK_SYNC_HTTP_EVENT_LOCK_KEY = "vikadata:dingtalk:event:lock:{}:{}:{}:{}";

    /**
     * Get DingTalk distributed event key
     *
     * @param subscribeId subscriber
     * @param corpId enterprise
     * @param bizId business data id
     * @param bizType business type
     * @return String
     */
    public static String getDingTalkSyncHttpEventLockKey(String subscribeId, String corpId, String bizId,
            Integer bizType) {
        return StrUtil.format(DING_TALK_SYNC_HTTP_EVENT_LOCK_KEY, subscribeId, corpId, bizId, bizType);
    }
}
