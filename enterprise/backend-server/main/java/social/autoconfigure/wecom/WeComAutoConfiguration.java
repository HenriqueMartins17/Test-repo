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

package com.apitable.enterprise.social.autoconfigure.wecom;

import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.RABBIT_ARGUMENT_DLK;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.RABBIT_ARGUMENT_DLX;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.SOCIAL_ISV_EVENT_EXCHANGE;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.SOCIAL_ISV_WECOM_ROUTING_KEY;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_EVENT_QUEUE;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_EVENT_TOPIC_QUEUE_BUFFER;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_EVENT_TOPIC_QUEUE_DEAD;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_EVENT_TOPIC_ROUTING_KEY;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_PERMIT_TOPIC_QUEUE_BUFFER;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_PERMIT_TOPIC_QUEUE_DEAD;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_ISV_PERMIT_TOPIC_ROUTING_KEY;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_TOPIC_EXCHANGE_BUFFER;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.WECOM_TOPIC_EXCHANGE_DEAD;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.apitable.enterprise.social.autoconfigure.wecom.WeComProperties.ConfigStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vikadata.social.wecom.WeComConfig;
import com.vikadata.social.wecom.WeComConfig.InitMenu;
import com.vikadata.social.wecom.WeComConfig.IsvApp;
import com.vikadata.social.wecom.WeComConfig.OperateEnpDdns;
import com.vikadata.social.wecom.WeComTemplate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.cp.api.WxCpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>
 * autoconfiguration of wecom
 * </p>
 *
 * @author Pengap
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WeComProperties.class)
@ConditionalOnClass(WxCpService.class)
@ConditionalOnProperty(value = "social.wecom.enabled", havingValue = "true")
public class WeComAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeComAutoConfiguration.class);

    private final WeComProperties wxMaProperties;

    public WeComAutoConfiguration(WeComProperties wxMaProperties) {
        this.wxMaProperties = wxMaProperties;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "social.wecom.enabled", havingValue = "true")
    protected static class WeComQueueCreator {

        /**
         * wecom buffer exchange
         */
        @Bean("weComBufferExchange")
        public TopicExchange weComBufferExchange() {
            return new TopicExchange(WECOM_TOPIC_EXCHANGE_BUFFER);
        }

        /**
         * wecom dead exchange
         */
        @Bean("weComDeadExchange")
        public TopicExchange weComDeadExchange() {
            return new TopicExchange(WECOM_TOPIC_EXCHANGE_DEAD);
        }

        /**
         * wecom isv event buffer queue
         */
        @Bean
        public Queue weComIsvEventBufferQueue() {
            return QueueBuilder.durable(WECOM_ISV_EVENT_TOPIC_QUEUE_BUFFER)
                .withArgument(RABBIT_ARGUMENT_DLX, WECOM_TOPIC_EXCHANGE_DEAD)
                .withArgument(RABBIT_ARGUMENT_DLK, WECOM_ISV_EVENT_TOPIC_ROUTING_KEY)
                .build();
        }

        /**
         * wecom isv event dead queue
         */
        @Bean("weComIsvEventDeadQueue")
        public Queue weComIsvEventDeadQueue() {
            return new Queue(WECOM_ISV_EVENT_TOPIC_QUEUE_DEAD);
        }

        /**
         * bind wecom isv event buffer to exchange
         */
        @Bean
        public Binding bindWeComBufferExchangeWithIsvEventQueue(
            @Qualifier("weComIsvEventBufferQueue") Queue weComIsvEventBufferQueue,
            @Qualifier("weComBufferExchange") TopicExchange weComBufferExchange) {
            return BindingBuilder.bind(weComIsvEventBufferQueue)
                .to(weComBufferExchange)
                .with(WECOM_ISV_EVENT_TOPIC_ROUTING_KEY);
        }

        /**
         * bind wecom isv event dead queue to exchange
         */
        @Bean
        public Binding bindWeComDeadExchangeWithIsvEventQueue(
            @Qualifier("weComIsvEventDeadQueue") Queue weComIsvEventDeadQueue,
            @Qualifier("weComDeadExchange") TopicExchange weComDeadExchange) {
            return BindingBuilder.bind(weComIsvEventDeadQueue)
                .to(weComDeadExchange)
                .with(WECOM_ISV_EVENT_TOPIC_ROUTING_KEY);
        }

        /**
         * wecom isv license permit buffer queue
         */
        @Bean("weComIsvPermitBufferQueue")
        public Queue weComIsvPermitBufferQueue() {
            return QueueBuilder.durable(WECOM_ISV_PERMIT_TOPIC_QUEUE_BUFFER)
                .withArgument(RABBIT_ARGUMENT_DLX, WECOM_TOPIC_EXCHANGE_DEAD)
                .withArgument(RABBIT_ARGUMENT_DLK, WECOM_ISV_PERMIT_TOPIC_ROUTING_KEY)
                .build();
        }

        /**
         * wecom isv license permit dead queue
         */
        @Bean("weComIsvPermitDeadQueue")
        public Queue weComIsvPermitDeadQueue() {
            return new Queue(WECOM_ISV_PERMIT_TOPIC_QUEUE_DEAD);
        }

        /**
         * bind wecom isv license permit to exchange
         */
        @Bean
        public Binding bindWeComBufferExchangeWithIsvPermitQueue(
            @Qualifier("weComIsvPermitBufferQueue") Queue weComIsvPermitBufferQueue,
            @Qualifier("weComBufferExchange") TopicExchange weComBufferExchange) {
            return BindingBuilder.bind(weComIsvPermitBufferQueue)
                .to(weComBufferExchange)
                .with(WECOM_ISV_PERMIT_TOPIC_ROUTING_KEY);
        }

        /**
         * bind wecom isv license permit dead queue to exchange
         */
        @Bean
        public Binding bindWeComDeadExchangeWithIsvPermitQueue(
            @Qualifier("weComIsvPermitDeadQueue") Queue weComIsvPermitDeadQueue,
            @Qualifier("weComDeadExchange") TopicExchange weComDeadExchange) {
            return BindingBuilder.bind(weComIsvPermitDeadQueue)
                .to(weComDeadExchange)
                .with(WECOM_ISV_PERMIT_TOPIC_ROUTING_KEY);
        }

        @Bean("socialIsvEventExchange")
        @ConditionalOnMissingBean(name = "socialIsvEventExchange")
        TopicExchange socialIsvEventExchange() {
            return new TopicExchange(SOCIAL_ISV_EVENT_EXCHANGE);
        }

        /**
         * init wecom isv event queue
         */
        @Bean("wecomIsvEventQueue")
        public Queue wecomIsvEventQueue() {
            return new Queue(WECOM_ISV_EVENT_QUEUE);
        }

        @Bean
        public Binding bindWecomIsvEventExchange(
            @Qualifier("wecomIsvEventQueue") Queue wecomIsvEventQueue,
            @Qualifier("socialIsvEventExchange") TopicExchange socialIsvEventExchange) {
            return BindingBuilder.bind(wecomIsvEventQueue)
                .to(socialIsvEventExchange)
                .with(SOCIAL_ISV_WECOM_ROUTING_KEY);

        }
    }

    @Bean
    @ConditionalOnMissingBean
    public WeComTemplate weComTemplate(ApplicationContext applicationContext) {
        ConfigStorage configStorage = wxMaProperties.getConfigStorage();
        List<InitMenu> initMenus = null;
        try {
            if (CollUtil.isNotEmpty(wxMaProperties.getInitMenus())) {
                ObjectMapper objectMapper = new ObjectMapper();
                String initMenusJson =
                    objectMapper.writeValueAsString(wxMaProperties.getInitMenus());
                TypeReference<List<InitMenu>> typeReference = new TypeReference<>() {
                };
                initMenus = objectMapper.readValue(initMenusJson, typeReference);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("parse wecom properties error", e);
        }

        OperateEnpDdns operateEnpDdns = new OperateEnpDdns();
        BeanUtil.copyProperties(wxMaProperties.getOperateEnpDdns(), operateEnpDdns,
            CopyOptions.create().ignoreError());

        WeComConfig config =
            new WeComConfig(configStorage.getStorageType().name(), configStorage.getKeyPrefix());
        config.setVikaWeComAppId(wxMaProperties.getVikaWeComAppId());
        config.setInitMenus(initMenus);
        config.setAutoCreateDomain(wxMaProperties.isAutoCreateDomain());
        config.setOperateEnpDdns(operateEnpDdns);
        config.setIsvAppList(Optional.ofNullable(wxMaProperties.getIsvAppList())
            .map(list -> list.stream()
                .map(item -> {
                    IsvApp isvApp = new IsvApp();
                    BeanUtil.copyProperties(item, isvApp);

                    return isvApp;
                }).collect(Collectors.toList()))
            .orElse(null));
        StringRedisTemplate stringRedisTemplate =
            applicationContext.getBean(StringRedisTemplate.class);
        WxRedisOps wxRedisOps =
            new RedisTemplateWxRedisOps(stringRedisTemplate);
        return new WeComTemplate(config, stringRedisTemplate, wxRedisOps);
    }
}
