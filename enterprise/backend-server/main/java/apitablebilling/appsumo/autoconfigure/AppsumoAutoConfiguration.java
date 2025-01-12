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

package com.apitable.enterprise.apitablebilling.appsumo.autoconfigure;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.appsumo.annotation.AppsumoEventHandler;
import com.apitable.enterprise.apitablebilling.appsumo.core.AppsumoTemplate;
import com.apitable.enterprise.apitablebilling.appsumo.handler.IAppsumoEventHandler;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * appsumo autoconfiguration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "appsumo.enabled", havingValue = "true")
@EnableConfigurationProperties(AppsumoProperties.class)
public class AppsumoAutoConfiguration implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppsumoAutoConfiguration.class);

    private final AppsumoProperties properties;
    private ApplicationContext applicationContext;

    public AppsumoAutoConfiguration(AppsumoProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * AppsumoTemplate.
     *
     * @return AppsumoTemplate
     */
    @Bean
    public AppsumoTemplate appsumoTemplate() {
        if (StrUtil.isBlank(properties.getAppId())) {
            throw new RuntimeException("Please Set Appsumo App Id");
        }
        return new AppsumoTemplate(properties.getAppId(), properties.getAppSecret());
    }

    /**
     * appsumo manager.
     *
     * @return AppsumoEventHandlerManager
     */
    @Bean
    public AppsumoEventHandlerManager appsumoEventHandlerManager() {
        return new AppsumoEventHandlerManager();
    }

    /**
     * app started listener.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void scanAppsumoEventHandler() {
        LOGGER.info("Starting scan appsumo event handler");
        AppsumoEventHandlerManager manager = AppsumoEventHandlerManager.me();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(AppsumoEventHandler.class);
        if (beans.isEmpty()) {
            LOGGER.warn("It seems that you do not provide the processing mechanism of appsumo event handler. is it correct？");
        }
        LOGGER.info("--------------------- Enable appsumo events ---------------------");
        for (Map.Entry<String, Object> bean : beans.entrySet()) {
            AppsumoEventHandler annotation = AopUtils.getTargetClass(bean.getValue()).getAnnotation(AppsumoEventHandler.class);
            manager.addHandler(annotation.action(), (IAppsumoEventHandler) bean.getValue());
            LOGGER.info("Event Name:{}", annotation.action().getAction());
        }
        LOGGER.info("-------------------------------------------------------------------");
    }


}
