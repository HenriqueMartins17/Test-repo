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

package com.apitable.enterprise.automation.autoconfigure;

import java.util.HashMap;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Automation delay queue config use for scheduled trigger.
 */
@Configuration(proxyBeanMethods = false)
public class AutomationQueueConfig {

    public static final String AUTOMATION_EVENT_EXCHANGE = "automation@exchange";

    public static final String AUTOMATION_EVENT_DELAY_QUEUE = "automation@delay";

    public static final String AUTOMATION_EVENT_RELEASE_QUEUE = "automation@release";

    public static final String AUTOMATION_EVENT_DEAD_ROUTING_KEY = "automation.dead";

    public static final String AUTOMATION_EVENT_CREATE_ROUTING_KEY = "automation.create";



    /**
     * create automation DLX exchange.
     * @return TopicExchange
     */
    @Bean("automationEventExchange")
    TopicExchange automationEventExchange() {
        return new TopicExchange(AUTOMATION_EVENT_EXCHANGE);
    }

    /**
     * create automation event delay queue.
     * @return queue
     */
    @Bean("automationEventDelayQueue")
    public Queue automationEventDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", AUTOMATION_EVENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", AUTOMATION_EVENT_DEAD_ROUTING_KEY);
        // 60s
        arguments.put("x-message-ttl", 60000);
        return new Queue(AUTOMATION_EVENT_DELAY_QUEUE, true, false,
            false, arguments);
    }

    /**
     * create automation event consumer queue.
     * @return queue
     */
    @Bean("automationEventReleaseQueue")
    public Queue automationEventReleaseQueue() {
        return new Queue(AUTOMATION_EVENT_RELEASE_QUEUE);
    }

    /**
     * Bind delay queue.
     * @return binding
     */
    @Bean("automationEventDelayBinding")
    Binding automationEventDelayBinding(
        @Qualifier("automationEventDelayQueue") Queue automationEventDelayQueue,
        @Qualifier("automationEventExchange") TopicExchange automationEventExchange) {
        return BindingBuilder.bind(automationEventDelayQueue)
            .to(automationEventExchange)
            .with(AUTOMATION_EVENT_CREATE_ROUTING_KEY);
    }

    /**
     * Bind release queue.
     * @return binding
     */
    @Bean("automationEventReleaseBinding")
    Binding automationEventReleaseBinding(
        @Qualifier("automationEventReleaseQueue") Queue automationEventReleaseQueue,
        @Qualifier("automationEventExchange") TopicExchange automationEventExchange) {
        return BindingBuilder.bind(automationEventReleaseQueue)
            .to(automationEventExchange)
            .with(AUTOMATION_EVENT_DEAD_ROUTING_KEY);
    }
}
