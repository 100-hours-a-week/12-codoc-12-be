package _ganzi.codoc.notification.dto;

import _ganzi.codoc.notification.domain.Notification;
import _ganzi.codoc.notification.enums.LinkCode;
import _ganzi.codoc.notification.enums.NotificationType;
import java.time.Instant;
import java.util.Map;

public record NotificationItem(
        Long notificationId,
        NotificationType type,
        String title,
        String body,
        LinkCode linkCode,
        Map<String, String> linkParams,
        Instant createdAt) {

    public static NotificationItem from(Notification notification) {
        return new NotificationItem(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLinkCode(),
                notification.getLinkParams(),
                notification.getCreatedAt());
    }
}
