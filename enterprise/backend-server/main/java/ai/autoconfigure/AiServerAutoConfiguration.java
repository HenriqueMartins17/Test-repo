package com.apitable.enterprise.ai.autoconfigure;

import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_DELAY_QUEUE_NAME;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN_DELAY;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN_FAILED;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN_RETRY;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_FAILED_QUEUE_NAME;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_QUEUE_NAME;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_RETRY_QUEUE_NAME;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_ROUTE_KEY;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_TRANSACTION_ROUTE_KEY;

import com.apitable.enterprise.ai.server.AiServer;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Server Auto configuration.
 *
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AiServerProperties.class)
@ConditionalOnProperty(value = "ai.enabled", havingValue = "true")
public class AiServerAutoConfiguration {

    private final AiServerProperties properties;

    public AiServerAutoConfiguration(AiServerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public InitAi initAi() {
        return new InitAi(properties);
    }

    @Bean("mainExchange")
    TopicExchange mainExchange() {
        return new TopicExchange(TRAINING_EXCHANGE_MAIN);
    }

    @Bean("delayExchange")
    TopicExchange delayExchange() {
        return new TopicExchange(TRAINING_EXCHANGE_MAIN_DELAY);
    }

    @Bean("retryExchange")
    TopicExchange retryExchange() {
        return new TopicExchange(TRAINING_EXCHANGE_MAIN_RETRY);
    }

    @Bean("failedExchange")
    TopicExchange failedExchange() {
        return new TopicExchange(TRAINING_EXCHANGE_MAIN_FAILED);
    }

    @Bean("mainQueue")
    Queue mainQueue() {
        return new Queue(TRAINING_QUEUE_NAME);
    }

    @Bean("delayQueue")
    Queue delayQueue() {
        return QueueBuilder.durable(TRAINING_DELAY_QUEUE_NAME)
            .deadLetterExchange(TRAINING_EXCHANGE_MAIN)
            .deadLetterRoutingKey(TRAINING_TRANSACTION_ROUTE_KEY)
            .ttl(60 * 1000)
            .build();
    }

    @Bean("retryQueue")
    Queue retryQueue() {
        return QueueBuilder.durable(TRAINING_RETRY_QUEUE_NAME)
            .deadLetterExchange(TRAINING_EXCHANGE_MAIN)
            .deadLetterRoutingKey(TRAINING_TRANSACTION_ROUTE_KEY)
            .ttl(60 * 1000)
            .build();
    }

    @Bean("failedQueue")
    Queue failedQueue() {
        return new Queue(TRAINING_FAILED_QUEUE_NAME);
    }

    @Bean
    Binding mainBinding(
        @Qualifier("mainQueue") Queue mainQueue,
        @Qualifier("mainExchange") TopicExchange mainExchange) {
        return BindingBuilder.bind(mainQueue)
            .to(mainExchange)
            .with(TRAINING_ROUTE_KEY);
    }

    @Bean
    Binding delayBinding(
        @Qualifier("delayQueue") Queue delayQueue,
        @Qualifier("delayExchange") TopicExchange delayExchange) {
        return BindingBuilder.bind(delayQueue)
            .to(delayExchange)
            .with(TRAINING_ROUTE_KEY);
    }

    @Bean
    Binding retryBinding(
        @Qualifier("retryQueue") Queue retryQueue,
        @Qualifier("retryExchange") TopicExchange retryExchange) {
        return BindingBuilder.bind(retryQueue)
            .to(retryExchange)
            .with(TRAINING_ROUTE_KEY);
    }

    @Bean
    Binding failedBinding(
        @Qualifier("failedQueue") Queue failedQueue,
        @Qualifier("failedExchange") TopicExchange failedExchange) {
        return BindingBuilder.bind(failedQueue)
            .to(failedExchange)
            .with(TRAINING_ROUTE_KEY);
    }

    /**
     * initialize ai server setting.
     */
    public static class InitAi implements InitializingBean {

        private final AiServerProperties properties;

        public InitAi(AiServerProperties properties) {
            this.properties = properties;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            AiServer.overrideBaseUrl(properties.getServerUrl());
        }
    }
}
