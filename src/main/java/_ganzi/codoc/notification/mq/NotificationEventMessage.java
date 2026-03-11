package _ganzi.codoc.notification.mq;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import java.time.Instant;
import java.util.Map;

public record NotificationEventMessage(
        String messageId,
        Long userId,
        NotificationType type,
        String title,
        String body,
        Map<String, String> linkParams,
        Instant occurredAt) {

    public NotificationMessageItem toNotificationMessageItem() {
        return new NotificationMessageItem(type, title, body, linkParams);
    }
}
