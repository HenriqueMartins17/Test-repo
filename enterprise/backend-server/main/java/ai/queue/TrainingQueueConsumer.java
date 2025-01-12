package com.apitable.enterprise.ai.queue;

import com.apitable.enterprise.ai.constants.AiConstants;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.server.Trainer;
import com.apitable.enterprise.ai.server.model.Training;
import com.apitable.enterprise.ai.server.model.TrainingInfo;
import com.apitable.enterprise.ai.service.IAiService;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Training queue consumer.
 */
@Service
@Slf4j
@ConditionalOnBean(name = {"mainQueue"})
public class TrainingQueueConsumer {

    @Resource
    private IAiService iAiService;

    /**
     * handle training status trace.
     *
     * @param queueMessage queue message
     * @param message      message
     * @param channel      channel
     * @throws IOException io exception
     */
    @RabbitListener(queues = AiConstants.TRAINING_QUEUE_NAME, errorHandler = "trainingTraceListenerErrorHandler")
    public void handleTrainingStatusTrace(TrainingMessage queueMessage,
                                          Message message,
                                          Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("attempt times {}, time: {}, received message: {}, ",
            deliveryTag,
            LocalDateTime.now(),
            queueMessage);
        TrainingInfo trainingInfo = Trainer.getTrainingInfo(queueMessage.getAiId(),
            queueMessage.getTrainingId());
        if (trainingInfo == null) {
            // give error handler to reject and requeue message back to queue
            throw new RuntimeException(
                String.format("mq process: not found training info, message: %s", queueMessage));
        }
        Training training = new Training(trainingInfo);
        TrainingStatus status = training.getStatus();
        if (status == TrainingStatus.SUCCESS) {
            iAiService.createQueryTransaction(trainingInfo.getAiId(), training,
                queueMessage.getUserId());
            channel.basicAck(deliveryTag, false);
            return;
        }
        if (status == TrainingStatus.FAILED) {
            // give error handler to reject and requeue message back to queue
            throw new RuntimeException(
                String.format("mq process: training is failure, message: %s", queueMessage));
        }
        // give error handler to reject and requeue message back to queue
        throw new RuntimeException(
            String.format("mq process: continue trace training status, message: %s", queueMessage));
    }
}
