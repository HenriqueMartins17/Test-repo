package com.apitable.enterprise.automation;

import static com.apitable.enterprise.automation.autoconfigure.AutomationQueueConfig.AUTOMATION_EVENT_CREATE_ROUTING_KEY;
import static com.apitable.enterprise.automation.autoconfigure.AutomationQueueConfig.AUTOMATION_EVENT_EXCHANGE;

import cn.hutool.core.lang.Dict;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.starter.amqp.core.RabbitSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AutomationQueueDefineTest extends AbstractVikaSaasIntegrationTest {

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @Test
    void testSendDelayMessage() {
        // 60s
        Dict dict = new Dict().set("message", "test");
        dict.set("ttl", "60s");
        rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
            AUTOMATION_EVENT_CREATE_ROUTING_KEY, dict, Integer.toString(60000));
        // 10s
        dict.set("ttl", "10s");
        rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
            AUTOMATION_EVENT_CREATE_ROUTING_KEY, dict, Integer.toString(10000));
        // 20s
        dict.set("ttl", "20s");
        rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
            AUTOMATION_EVENT_CREATE_ROUTING_KEY, dict, Integer.toString(20000));
        // 1s
        dict.set("ttl", "1s");
        rabbitSenderService.topicSend(AUTOMATION_EVENT_EXCHANGE,
            AUTOMATION_EVENT_CREATE_ROUTING_KEY, dict, Integer.toString(1000));
    }
}
