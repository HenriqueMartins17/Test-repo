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

import cn.hutool.json.JSONUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Getter;
import lombok.Setter;

import com.vikadata.scheduler.space.service.IDatasheetMetaService;

import org.springframework.stereotype.Component;

/**
 * <p>
 * Clear One Way Link Job Handler
 * </p>
 */
@Component
public class ClearOneWayLinkJobHandler {

    @Resource
    private IDatasheetMetaService iDatasheetMetaService;

    @XxlJob(value = "clearSingleAssociation")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("Job. param:{}", param);
        JobParam jobParam = JSONUtil.toBean(param, JobParam.class);

        iDatasheetMetaService.oneWayLinkDataHandler(jobParam);
    }

    /**
     * Online full space station scan and repair is not supported for now,
     * * because the pressure on the database is a bit large,
     * * and it can only operate on designated space stations during online operation*
     */
    @Getter
    @Setter
    public static class JobParam {

        public enum RunFunc {
            // List one-way correlation exception data without any processing operation (default)
            LIST,
            // Clean up one-way associated data, clean up the data
            HANDLE,
            // Read the remote data stream and clean the data
            READ_REMOTE_STREAM,
        }

        // run model
        private RunFunc runFunc = RunFunc.LIST;

        private String spaceId;

        /*
         * remote stream Url(resource relative address)
         * example：
         * full URL：https://xxx.com/job/analyze/association/result/main-2022-02-24%2014%3A28%3A08.json
         * relative address: job/analyze/association/result/main-2022-02-24%2014%3A28%3A08.json
         */
        private String readRemoteStreamUrl;

        private int coreQueryPoolSize = 8;

        private int coreAnalyzePoolSize = 64;

        private long pageSize = 10000L;

        /**
         * execution interval
         * default:500ms
         */
        private long executionInterval = 500L;
    }

}
