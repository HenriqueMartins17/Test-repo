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

package com.vikadata.schedule.space.service;

import javax.annotation.Resource;

import cn.hutool.core.lang.ConsoleTable;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.vikadata.scheduler.space.SchedulerSpaceApplication;
import com.vikadata.scheduler.space.handler.RoomIpHealthIndicatorJobHandler;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ActiveProfiles;

@Disabled("no assertion")
@Slf4j
@SpringBootTest(classes = SchedulerSpaceApplication.class)
@EnableScheduling
@ActiveProfiles("local")
public class RoomIpHealthIndicatorTest {

    @Resource
    private RoomIpHealthIndicatorJobHandler roomIpHealthIndicatorJobHandler;

    @Scheduled(cron = "*/3 * * * * ?")
    public void execute() {
        roomIpHealthIndicatorJobHandler.execute();
    }

    @Test
    public void testMain() {
        try {
            Thread.sleep(30000000L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Disabled("no assertion")
    public static class JobTest {

        @Test
        public void test1() {
            ConsoleTable consoleTable = ConsoleTable.create()
                    .addHeader("　Room Ip List")
                    .addBody("192.168.111.111")
                    .addBody("192.168.1.2");
            System.out.println(consoleTable.toString());
        }

    }

}
