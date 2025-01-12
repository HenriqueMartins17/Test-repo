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

package com.vikadata.social.service.dingtalk.service;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.vikadata.social.dingtalk.model.DingTalkUserListResponse;
import com.vikadata.social.service.dingtalk.SocialDingTalkApplication;

import org.springframework.boot.test.context.SpringBootTest;

@Disabled("no assertion")
@Slf4j
@SpringBootTest(classes = SocialDingTalkApplication.class)
public class DingTalkTest {

    @Resource
    private IDingTalkService iDingTalkService;

    @Test
    public void testApiLimitPool() {
        // Test DingTalk api current limit
        ThreadPoolExecutor threadPoolExecutor = ThreadUtil.newExecutorByBlockingCoefficient((float) 0.9f);
        log.info("Number of threads: {}", threadPoolExecutor.getCorePoolSize());
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 50; i++) {
            threadPoolExecutor.execute(() -> {
                DingTalkUserListResponse response = iDingTalkService.getUserDetailList("20303001",
                        "dinga39a6d188d0e7fddbc961a6cb783455b", 1L, 0,
                        100);
                log.info("Return data: {}", JSONUtil.toJsonStr(response));
            });
        }
        ThreadUtil.sleep(10000000);
    }
}
