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

package com.vikadata.scheduler.space.handler;

import javax.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import com.vikadata.scheduler.space.service.IUserService;

import org.springframework.stereotype.Component;

@Component
public class UserJobHandler {

    /**
     * The default number of days for the cooling-off period is 30 days,
     * and the account will be closed if the cancellation is not cancelled after this period.
     */
    private final static Integer DEFAULT_PAUSE_LIMIT_DAYS = 30;

    private final static String PAUSE_LIMIT_DAYS_KEY_NAME = "PAUSE_LIMIT_DAYS";

    @Resource
    private IUserService userService;

    /**
     * Close the cooling-o`ff account
     */
    @XxlJob(value = "closePausedUserJobHandler")
    public void closePausedUserJobHandler() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("param:{}", param);
        int pausedLimitDays = this.getPauseLimitDays(param);
        userService.closePausedUser(pausedLimitDays);
    }

    private int getPauseLimitDays(String param) {
        if (StrUtil.isNotBlank(param)) {
            JSONObject obj = JSONUtil.parseObj(param);
            return obj.getInt(PAUSE_LIMIT_DAYS_KEY_NAME, DEFAULT_PAUSE_LIMIT_DAYS);
        }
        return DEFAULT_PAUSE_LIMIT_DAYS;
    }
}
