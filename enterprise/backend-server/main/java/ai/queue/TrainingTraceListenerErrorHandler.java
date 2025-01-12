package com.apitable.enterprise.ai.queue;

import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN_FAILED;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN_RETRY;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_TRANSACTION_ROUTE_KEY;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

/**
 * training trace queue listener error handler.
 */
@Component
@Slf4j
public class TrainingTraceListenerErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws IOException {
        if (message == null) {
            log.error("mq catch exception: message is null");
            return null;
        }
        Channel channel = (Channel) message.getHeaders().get(AmqpHeaders.CHANNEL);
        if (channel == null) {
            log.error("mq catch exception: channel is null");
            return null;
        }
        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        if (deliveryTag == null) {
            log.error("mq catch exception: deliveryTag is null");
            return null;
        }
        MessageProperties messageProperties = amqpMessage.getMessageProperties();
        long retryCount = getRetryCount(messageProperties);
        log.info("retry count: {}", retryCount);
        if (retryCount > 60) {
            log.error("mq catch exception: send message to failed queue");
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-orig-routing-key",
                getOrigRoutingKey(messageProperties, messageProperties.getReceivedRoutingKey()));
            channel.basicPublish(TRAINING_EXCHANGE_MAIN_FAILED,
                TRAINING_TRANSACTION_ROUTE_KEY,
                createOverrideProperties(messageProperties, headers),
                amqpMessage.getBody()
            );
        } else {
            log.info("mq catch exception: send message to retry queue");
            Map<String, Object> headers = messageProperties.getHeaders();
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put("x-orig-routing-key",
                getOrigRoutingKey(messageProperties, messageProperties.getReceivedRoutingKey()));
            channel.basicPublish(TRAINING_EXCHANGE_MAIN_RETRY,
                TRAINING_TRANSACTION_ROUTE_KEY,
                createOverrideProperties(messageProperties, headers),
                amqpMessage.getBody()
            );
        }
        channel.basicAck(deliveryTag, false);
        return null;
    }

    protected Long getRetryCount(MessageProperties properties) {
        Long retryCount = 0L;
        try {
            List<Map<String, ?>> deaths = properties.getXDeathHeader();
            if (deaths != null && !deaths.isEmpty()) {
                Map<String, ?> death = deaths.iterator().next();
                retryCount = (Long) death.get("count");
            }
        } catch (Exception ignored) {
            // ignore
        }

        return retryCount;
    }

    protected String getOrigRoutingKey(MessageProperties properties, String defaultValue) {
        String routingKey = defaultValue;
        try {
            Map<String, Object> headers = properties.getHeaders();
            if (headers != null) {
                if (headers.containsKey("x-orig-routing-key")) {
                    routingKey = headers.get("x-orig-routing-key").toString();
                }
            }
        } catch (Exception ignored) {
            // ignore
        }

        return routingKey;
    }

    protected AMQP.BasicProperties createOverrideProperties(MessageProperties properties,
                                                            Map<String, Object> headers) {
        return new AMQP.BasicProperties(
            properties.getContentType(),
            properties.getContentEncoding(),
            headers,
            properties.getReceivedDeliveryMode().ordinal() + 1,
            properties.getPriority(),
            properties.getCorrelationId(),
            properties.getReplyTo(),
            properties.getExpiration(),
            properties.getMessageId(),
            properties.getTimestamp(),
            properties.getType(),
            properties.getUserId(),
            properties.getAppId(),
            properties.getClusterId()
        );
    }
}
