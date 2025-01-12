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
 * Fix Datasheet DataH andler
 * </p>
 */
@Component
public class FixDatasheetDataHandler {

    @Resource
    private IDatasheetMetaService iDatasheetMetaService;

    @XxlJob(value = "fixDatasheetData")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("Job. param:{}", param);
        JobParam jobParam = JSONUtil.toBean(param, JobParam.class);

        switch (jobParam.getFixDataMode()) {
            case FIX_TEMPLATE_VIEW_SORTINFO:
                iDatasheetMetaService.fixTemplateViewSortInfo(jobParam);
                break;
            default:
                XxlJobHelper.log("Fix Data Mode Undefined");
                break;
        }
    }

    @Getter
    @Setter
    public static class JobParam {
        enum FixDataMode {
            // fix number table view sort field
            FIX_TEMPLATE_VIEW_SORTINFO
        }

        private FixDataMode fixDataMode;

        private String spaceId;

        private String dstId;
    }

}
