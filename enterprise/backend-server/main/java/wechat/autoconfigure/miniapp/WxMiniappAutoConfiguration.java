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

package com.apitable.enterprise.wechat.autoconfigure.miniapp;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceHttpClientImpl;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceJoddHttpImpl;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceOkHttpImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import org.apache.commons.lang3.StringUtils;

import com.apitable.enterprise.wechat.autoconfigure.miniapp.WxMaProperties.HttpClientType;

import org.springframework.beans.BeansException;
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
@EnableConfigurationProperties(WxMaProperties.class)
@ConditionalOnClass(WxMaService.class)
@ConditionalOnProperty(value = "wx.miniapp.enabled", havingValue = "true")
public class WxMiniappAutoConfiguration {

    private final WxMaProperties wxMaProperties;

    public WxMiniappAutoConfiguration(WxMaProperties wxMaProperties) {
        this.wxMaProperties = wxMaProperties;
    }

    @Configuration(proxyBeanMethods = false)
    protected static class StorageAutoConfiguration implements ApplicationContextAware {

        private final WxMaProperties wxMaProperties;

        private ApplicationContext applicationContext;

        public StorageAutoConfiguration(WxMaProperties wxMaProperties) {
            this.wxMaProperties = wxMaProperties;
        }

        @Bean
        @ConditionalOnProperty(value = "wx.miniapp.config-storage.storage-type", havingValue = "memory")
        public WxMaConfig wxMaInMemoryStorageConfig() {
            return config(new WxMaDefaultConfigImpl(), wxMaProperties);
        }

        @Bean
        @ConditionalOnProperty(value = "wx.miniapp.config-storage.storage-type", havingValue = "redistemplate")
        public WxMaConfig wxMaInRedisTemplateStorageConfig() {
            StringRedisTemplate redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
            WxRedisOps redisOps = new RedisTemplateWxRedisOps(redisTemplate);
            return config(new WxMaRedisBetterConfigImpl(redisOps, wxMaProperties.getConfigStorage().getKeyPrefix()), wxMaProperties);
        }

        private WxMaDefaultConfigImpl config(WxMaDefaultConfigImpl config, WxMaProperties properties) {
            config.setAppid(StringUtils.trimToNull(properties.getAppId()));
            config.setSecret(StringUtils.trimToNull(properties.getSecret()));
            config.setToken(StringUtils.trimToNull(properties.getToken()));
            config.setAesKey(StringUtils.trimToNull(properties.getAesKey()));
            config.setMsgDataFormat(StringUtils.trimToNull(properties.getMsgDataFormat()));

            WxMaProperties.ConfigStorage configStorageProperties = properties.getConfigStorage();
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
    @ConditionalOnMissingBean(WxMaService.class)
    public WxMaService wxMaService(WxMaConfig wxMaConfig) {
        HttpClientType httpClientType = wxMaProperties.getConfigStorage().getHttpClientType();
        WxMaService wxMaService;
        switch (httpClientType) {
            case OkHttp:
                wxMaService = new WxMaServiceOkHttpImpl();
                break;
            case JoddHttp:
                wxMaService = new WxMaServiceJoddHttpImpl();
                break;
            case HttpClient:
                wxMaService = new WxMaServiceHttpClientImpl();
                break;
            default:
                wxMaService = new WxMaServiceImpl();
                break;
        }
        wxMaService.setWxMaConfig(wxMaConfig);
        return wxMaService;
    }
}
