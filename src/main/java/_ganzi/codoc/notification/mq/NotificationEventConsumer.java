package _ganzi.codoc.notification.mq;

import _ganzi.codoc.notification.config.NotificationMqProperties;
import _ganzi.codoc.notification.service.NotificationConsumeLogService;
import _ganzi.codoc.notification.service.NotificationConsumeLogService.ConsumeDecision;
import _ganzi.codoc.notification.service.NotificationSendService;
import _ganzi.codoc.notification.service.PushNotificationSendService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.mq", name = "enabled", havingValue = "true")
public class NotificationEventConsumer {

    private static final String CHANNEL_IN_APP = "IN_APP";
    private static final String CHANNEL_PUSH = "PUSH";

    private final NotificationMqProperties notificationMqProperties;
    private final NotificationSendService notificationSendService;
    private final PushNotificationSendService pushNotificationSendService;
    private final NotificationConsumeLogService notificationConsumeLogService;

    @RabbitListener(
            queues = "${app.notification.mq.in-app-queue}",
            containerFactory = "notificationRabbitListenerContainerFactory")
    public void consumeInApp(NotificationEventMessage event) {
        ConsumeDecision decision =
                notificationConsumeLogService.beginConsume(event.messageId(), CHANNEL_IN_APP);
        if (decision == ConsumeDecision.SKIP) {
            return;
        }
        executeWithRetry(
                event,
                CHANNEL_IN_APP,
                payload ->
                        notificationSendService.send(payload.userId(), payload.toNotificationMessageItem()));
    }

    @RabbitListener(
            queues = "${app.notification.mq.push-queue}",
            containerFactory = "notificationRabbitListenerContainerFactory")
    public void consumePush(NotificationEventMessage event) {
        ConsumeDecision decision =
                notificationConsumeLogService.beginConsume(event.messageId(), CHANNEL_PUSH);
        if (decision == ConsumeDecision.SKIP) {
            return;
        }
        executeWithRetry(
                event,
                CHANNEL_PUSH,
                payload ->
                        pushNotificationSendService.send(
                                payload.userId(), payload.toNotificationMessageItem()));
    }

    private void executeWithRetry(
            NotificationEventMessage event,
            String channel,
            Consumer<NotificationEventMessage> consumeAction) {
        int maxAttempts = Math.max(1, notificationMqProperties.retryMaxAttempts());
        long interval = Math.max(0L, notificationMqProperties.retryInitialIntervalMillis());
        double multiplier =
                notificationMqProperties.retryMultiplier() <= 0.0
                        ? 1.0
                        : notificationMqProperties.retryMultiplier();
        long maxInterval = Math.max(interval, notificationMqProperties.retryMaxIntervalMillis());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                consumeAction.accept(event);
                notificationConsumeLogService.markDone(event.messageId(), channel);
                return;
            } catch (Exception exception) {
                notificationConsumeLogService.markFailed(event.messageId(), channel, exception);
                if (attempt == maxAttempts) {
                    throw exception;
                }
                sleep(interval);
                interval = Math.min(maxInterval, Math.round(interval * multiplier));
            }
        }
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Notification consume retry interrupted", exception);
        }
    }
}
