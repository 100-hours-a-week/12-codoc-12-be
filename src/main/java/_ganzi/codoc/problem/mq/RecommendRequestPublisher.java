package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.config.RecommendMqProperties;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
public class RecommendRequestPublisher {

    @Qualifier("recommendRabbitTemplate")
    private final RabbitTemplate rabbitTemplate;

    private final RecommendMqProperties properties;

    public void publish(String jobId, RecommendRequest payload, Instant requestedAt) {
        RecommendRequestMessage message = new RecommendRequestMessage(jobId, requestedAt, payload);
        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.requestRoutingKey(),
                message,
                amqpMessage -> {
                    amqpMessage.getMessageProperties().setMessageId(jobId);
                    amqpMessage.getMessageProperties().setHeader("jobId", jobId);
                    return amqpMessage;
                },
                new CorrelationData(jobId));
        log.info("recommend request published. jobId={}", jobId);
    }
}
