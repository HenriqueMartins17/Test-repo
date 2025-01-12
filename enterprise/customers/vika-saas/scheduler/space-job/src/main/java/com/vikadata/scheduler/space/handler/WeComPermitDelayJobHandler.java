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

import java.util.Objects;

import javax.annotation.Resource;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import com.vikadata.scheduler.space.config.properties.InternalProperties;
import com.vikadata.scheduler.space.model.ResponseDataDto;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * WeCom Permit Delay Job Handler
 * </p>
 */
@Component
public class WeComPermitDelayJobHandler {

    @Resource
    private InternalProperties internalProperties;

    @Resource
    private RestTemplate restTemplate;

    @XxlJob(value = "callSocialWecomPermitDelayBatchProcess")
    public void callSocialWecomPermitDelayBatchProcess() {
        String url = internalProperties.getDomain() + internalProperties.getBatchProcessSocialWecomPermitDelayUrl();
        ResponseDataDto<?> response = restTemplate.postForObject(url, null, ResponseDataDto.class);
        if (Objects.isNull(response) || Boolean.FALSE.equals(response.getSuccess())) {
            XxlJobHelper.log("Failed to call batch processing enterprise micro interface license delay interface");
        }
    }

}
