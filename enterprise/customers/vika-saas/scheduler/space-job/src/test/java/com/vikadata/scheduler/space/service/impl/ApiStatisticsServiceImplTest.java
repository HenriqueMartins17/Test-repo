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

package com.vikadata.scheduler.space.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import cn.hutool.core.io.IoUtil;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Test;

import com.vikadata.AbstractIntegrationTest;
import com.vikadata.FileHelper;
import com.vikadata.scheduler.space.SchedulerSpaceApplication;
import com.vikadata.scheduler.space.mapper.developer.ApiStatisticsMapper;
import com.vikadata.scheduler.space.model.SpaceApiUsageDto;
import com.vikadata.scheduler.space.service.IApiStatisticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * <p>
 * Api Statistics Service Unit Test
 * <p>
 */
@Slf4j
@SpringBootTest(classes = SchedulerSpaceApplication.class)
public class ApiStatisticsServiceImplTest extends AbstractIntegrationTest {

    @Resource
    private ApiStatisticsMapper apiStatisticsMapper;

    @Resource
    private IApiStatisticsService apiStatisticsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void SyncApiUsageDailyData() {
        List<SpaceApiUsageDto> spaceApiUsageDtoList = new ArrayList<>();
        SpaceApiUsageDto spaceApiUsageDto = new SpaceApiUsageDto();
        spaceApiUsageDto.setSpaceId("spc4fjVmXhzs1");
        spaceApiUsageDto.setStatisticsTime("2022-05-27");
        spaceApiUsageDto.setTotalCount(100L);
        spaceApiUsageDto.setSuccessCount(99L);
        spaceApiUsageDtoList.add(spaceApiUsageDto);

        assertThat(apiStatisticsMapper.selectLastApiUsageDailyRecord()).isNull();
        apiStatisticsService.syncApiUsageDailyData(spaceApiUsageDtoList);
        assertThat(apiStatisticsMapper.selectLastApiUsageDailyRecord()).isNotNull();
    }

    @Test
    void SyncApiUsageMonthlyData() {
        List<SpaceApiUsageDto> spaceApiUsageDtoList = new ArrayList<>();
        SpaceApiUsageDto spaceApiUsageDto = new SpaceApiUsageDto();
        spaceApiUsageDto.setSpaceId("spc4fjVmXhzs1");
        spaceApiUsageDto.setStatisticsTime("2022-05");
        spaceApiUsageDto.setTotalCount(100L);
        spaceApiUsageDto.setSuccessCount(99L);
        spaceApiUsageDtoList.add(spaceApiUsageDto);

        assertThat(apiStatisticsMapper.selectLastApiUsageMonthlyRecord()).isNull();
        apiStatisticsService.syncApiUsageMonthlyData(spaceApiUsageDtoList);
        assertThat(apiStatisticsMapper.selectLastApiUsageMonthlyRecord()).isNotNull();
    }

    @Test
    void spaceApiUsageDailyStatistics() throws IOException {
        String resourceName = "testdata/api-usage-statistics-data.sql";
        InputStream inputStream = FileHelper.getInputStreamFromResource(resourceName);
        String sql = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        jdbcTemplate.execute(sql);

        assertThat(apiStatisticsMapper.selectLastApiUsageDailyRecord()).isNull();
        apiStatisticsService.spaceApiUsageDailyStatistics();
        assertThat(apiStatisticsMapper.selectLastApiUsageDailyRecord()).isNotNull();
    }

    @Test
    void spaceApiUsageMonthlyStatistics() throws ParseException, IOException {
        String resourceName = "testdata/api-usage-statistics-data.sql";
        InputStream inputStream = FileHelper.getInputStreamFromResource(resourceName);
        String sql = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        jdbcTemplate.execute(sql);

        assertThat(apiStatisticsMapper.selectLastApiUsageMonthlyRecord()).isNull();
        apiStatisticsService.spaceApiUsageMonthlyStatistics();
        assertThat(apiStatisticsMapper.selectLastApiUsageMonthlyRecord()).isNotNull();
    }
}
