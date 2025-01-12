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

package com.vikadata.scheduler.space.cache.service;

import javax.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.vikadata.scheduler.space.SchedulerSpaceApplication;

import org.springframework.boot.test.context.SpringBootTest;

@Disabled("no assertion")
@SpringBootTest(classes = SchedulerSpaceApplication.class)
public class RedisServiceTest {

    @Resource
    private RedisService redisService;

    @Test
    public void refreshApiUsageNextMonthMinId() {
        redisService.refreshApiUsageNextMonthMinId();
    }
}