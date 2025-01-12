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

package com.vikadata;

import java.util.List;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vikadata.scheduler.space.SchedulerSpaceApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest(classes = { SchedulerSpaceApplication.class })
@AutoConfigureMockMvc
@ExtendWith({ MockitoExtension.class })
@TestPropertySource(value = {
        "classpath:test.properties",
})
public abstract class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("#{'${exclude}'.split(',')}")
    private List<String> excludeTables;

    @BeforeEach
    void setup() {
        UnitTestUtil.clearDB(jdbcTemplate, excludeTables);
        UnitTestUtil.cleanCacheKeys(redisTemplate);
    }
}
