package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.config.RecommendMqProperties;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.CorrelationData.Confirm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
public class RecommendRequestPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RecommendMqProperties properties;

    public RecommendRequestPublisher(
            @Qualifier("recommendRabbitTemplate") RabbitTemplate rabbitTemplate,
            RecommendMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void initCallbacks() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    if (ack) {
                        return;
                    }
                    String jobId = correlationData == null ? null : correlationData.getId();
                    log.warn("recommend request publish nack. jobId={}, cause={}", jobId, cause);
                });
        rabbitTemplate.setReturnsCallback(
                returned -> {
                    String jobId =
                            (String) returned.getMessage().getMessageProperties().getHeaders().get("jobId");
                    log.warn(
                            "recommend request publish returned. jobId={}, replyCode={}, replyText={},"
                                    + " routingKey={}",
                            jobId,
                            returned.getReplyCode(),
                            returned.getReplyText(),
                            returned.getRoutingKey());
                });
    }

    public void publish(String jobId, RecommendRequest payload, Instant requestedAt) {
        publish(new RecommendRequestMessage(jobId, requestedAt, payload));
    }

    public void publish(RecommendRequestMessage message) {
        CorrelationData correlationData = new CorrelationData(message.jobId());
        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.requestRoutingKey(),
                message,
                amqpMessage -> {
                    amqpMessage.getMessageProperties().setMessageId(message.jobId());
                    amqpMessage.getMessageProperties().setHeader("jobId", message.jobId());
                    return amqpMessage;
                },
                correlationData);

        try {
            Confirm confirm = correlationData.getFuture().get(10, TimeUnit.SECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException(
                        "Recommend request publish nack. jobId="
                                + message.jobId()
                                + ", cause="
                                + confirm.reason());
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to confirm recommend request publish. jobId=" + message.jobId(), exception);
        }

        log.info("recommend request published. jobId={}", message.jobId());
    }
}
