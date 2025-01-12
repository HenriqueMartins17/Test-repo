/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.wechat.autoconfigure.open;

import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.open.api.WxOpenComponentService;
import me.chanjar.weixin.open.api.WxOpenConfigStorage;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenInMemoryConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenMessageRouter;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WxOpenProperties.class)
@ConditionalOnClass(WxOpenService.class)
@ConditionalOnProperty(value = "wx.open.enabled", havingValue = "true")
public class WxOpenAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    protected static class StorageAutoConfiguration implements ApplicationContextAware {

        private final WxOpenProperties wxOpenProperties;

        private ApplicationContext applicationContext;

        public StorageAutoConfiguration(WxOpenProperties wxOpenProperties) {
            this.wxOpenProperties = wxOpenProperties;
        }

        @Bean
        @ConditionalOnProperty(value = "wx.open.config-storage.storage-type", havingValue = "memory")
        public WxOpenConfigStorage wxOpenInMemoryStorageConfig() {
            WxOpenInMemoryConfigStorage config = new WxOpenInMemoryConfigStorage();
            return config(config, wxOpenProperties);
        }

        @Bean
        @ConditionalOnProperty(value = "wx.open.config-storage.storage-type", havingValue = "redistemplate")
        public WxOpenConfigStorage wxOpenInRedisTemplateStorageConfig() {
            StringRedisTemplate redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
            WxRedisOps redisOps = new RedisTemplateWxRedisOps(redisTemplate);
            return config(new WxOpenInRedisConfigStorage(redisOps, wxOpenProperties.getConfigStorage().getKeyPrefix()), wxOpenProperties);
        }

        private WxOpenInMemoryConfigStorage config(WxOpenInMemoryConfigStorage config, WxOpenProperties properties) {
            WxOpenProperties.ConfigStorage configStorageProperties = properties.getConfigStorage();
            config.setWxOpenInfo(properties.getAppId(), properties.getSecret(), properties.getToken(), properties.getAesKey());
            config.setHttpProxyHost(configStorageProperties.getHttpProxyHost());
            config.setHttpProxyUsername(configStorageProperties.getHttpProxyUsername());
            config.setHttpProxyPassword(configStorageProperties.getHttpProxyPassword());
            if (configStorageProperties.getHttpProxyPort() != null) {
                config.setHttpProxyPort(configStorageProperties.getHttpProxyPort());
            }
            return config;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    @Bean
    @ConditionalOnMissingBean(WxOpenService.class)
    public WxOpenService wxOpenService(WxOpenConfigStorage wxOpenConfigStorage) {
        WxOpenService wxOpenService = new WxOpenServiceImpl();
        wxOpenService.setWxOpenConfigStorage(wxOpenConfigStorage);
        return wxOpenService;
    }

    @Bean
    public WxOpenMessageRouter wxOpenMessageRouter(WxOpenService wxOpenService) {
        return new WxOpenMessageRouter(wxOpenService);
    }

    @Bean
    public WxOpenComponentService wxOpenComponentService(WxOpenService wxOpenService) {
        return wxOpenService.getWxOpenComponentService();
    }
}
