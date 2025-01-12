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

import java.text.ParseException;

import javax.annotation.Resource;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import com.vikadata.scheduler.space.cache.service.RedisService;
import com.vikadata.scheduler.space.service.IApiStatisticsService;
import com.vikadata.scheduler.space.service.ISpaceAssetService;
import com.vikadata.scheduler.space.service.ISpaceService;

import org.springframework.stereotype.Component;

/**
 * <p>
 * Space Job Handler
 * </p>
 */
@Component
public class SpaceJobHandler {

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ISpaceAssetService iSpaceAssetService;

    @Resource
    private RedisService redisService;

    @Resource
    private IApiStatisticsService apiStatisticsService;

    @XxlJob(value = "delSpaceJobHandler")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("Delete Space. param:{}", param);
        iSpaceService.delSpace(param);
    }

    @XxlJob(value = "refCountingJobHandler")
    public void refCounting() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("Ref Counting. param:{}", param);
        iSpaceAssetService.referenceCounting(param);
    }

    @XxlJob(value = "refreshApiUsageNextMonthIdHandler")
    public void refreshApiUsageNextMonthMinId() {
        redisService.refreshApiUsageNextMonthMinId();
    }

    @XxlJob(value = "spaceApiUsageDailyStatistics")
    public void spaceApiUsageDailyStatistics() {
        apiStatisticsService.spaceApiUsageDailyStatistics();
    }

    @XxlJob(value = "spaceApiUsageMonthlyStatistics")
    public void spaceApiUsageMonthlyStatistics() throws ParseException {
        apiStatisticsService.spaceApiUsageMonthlyStatistics();
    }
}
