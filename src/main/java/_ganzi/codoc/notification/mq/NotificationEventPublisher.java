package _ganzi.codoc.notification.mq;

import _ganzi.codoc.notification.config.NotificationMqProperties;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.mq", name = "enabled", havingValue = "true")
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationMqProperties properties;

    @PostConstruct
    public void initCallbacks() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    if (ack) {
                        return;
                    }
                    String messageId = correlationData == null ? null : correlationData.getId();
                    log.warn("notification publish nack. messageId={}, cause={}", messageId, cause);
                });
        rabbitTemplate.setReturnsCallback(
                returned -> {
                    String messageId =
                            (String) returned.getMessage().getMessageProperties().getHeaders().get("messageId");
                    log.warn(
                            "notification publish returned. messageId={}, replyCode={}, replyText={},"
                                    + " routingKey={}",
                            messageId,
                            returned.getReplyCode(),
                            returned.getReplyText(),
                            returned.getRoutingKey());
                });
    }

    public void publish(Long userId, NotificationMessageItem messageItem) {
        NotificationEventMessage event =
                new NotificationEventMessage(
                        UUID.randomUUID().toString(),
                        userId,
                        messageItem.type(),
                        messageItem.title(),
                        messageItem.body(),
                        messageItem.linkParams(),
                        Instant.now());
        String routingKey = "notification." + messageItem.type().name().toLowerCase();
        rabbitTemplate.convertAndSend(
                properties.exchange(),
                routingKey,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(event.messageId());
                    message.getMessageProperties().setHeader("messageId", event.messageId());
                    return message;
                },
                new CorrelationData(event.messageId()));
        log.info(
                "notification event published. messageId={}, userId={}, type={}",
                event.messageId(),
                userId,
                messageItem.type());
    }
}
