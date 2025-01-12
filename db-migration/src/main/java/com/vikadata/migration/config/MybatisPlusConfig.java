package com.vikadata.migration.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus 配置
 *
 * @author Shawn Deng
 * @date 2021-08-17 11:08:16
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 取消缓存
            configuration.setCacheEnabled(false);
        };
    }

    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            // 取消控制台横幅显示
            properties.getGlobalConfig().setBanner(false);
            // 开启检查XML文件
            properties.setCheckConfigLocation(true);
        };
    }
}
