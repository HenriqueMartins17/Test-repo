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

package com.vikadata.scheduler.space.config;

import java.util.Properties;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>
 * Mybatis-plus Config
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@MapperScan("com.vikadata.scheduler.space.mapper")
@Slf4j
public class MybatisPlusConfig {

    @Value("${DATABASE_TABLE_PREFIX:apitable_}")
    private String dbTablePrefix;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * MyBatis Config
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
            configuration.setCacheEnabled(false);
        };
    }

    /**
     * Mybatis Plus Config
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            properties.getGlobalConfig().setBanner(false);
            properties.getGlobalConfig().getDbConfig().setTablePrefix(dbTablePrefix);
            Properties customProperties = new Properties();
            customProperties.setProperty("tablePrefix", dbTablePrefix);
            properties.setConfigurationProperties(customProperties);
            properties.setCheckConfigLocation(true);
            properties.getGlobalConfig().getDbConfig().setUpdateStrategy(FieldStrategy.NOT_EMPTY);
        };
    }
}
