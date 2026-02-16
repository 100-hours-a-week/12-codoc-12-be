package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.dto.NotificationItem;
import _ganzi.codoc.notification.dto.NotificationResponse;
import _ganzi.codoc.notification.dto.NotificationUnreadStatusResponse;
import _ganzi.codoc.notification.repository.NotificationRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationService {

    private static final Duration INBOX_RETENTION = Duration.ofDays(7);

    private final NotificationRepository notificationRepository;

    public NotificationResponse getRecentNotifications(Long userId) {
        Instant cutoff = cutoff();

        List<NotificationItem> notifications =
                notificationRepository.findRecentByUserId(userId, cutoff).stream()
                        .map(NotificationItem::from)
                        .toList();

        return new NotificationResponse(notifications);
    }

    public NotificationUnreadStatusResponse getUnreadStatus(Long userId) {
        boolean hasUnread = notificationRepository.existsUnreadRecentByUserId(userId, cutoff());
        return new NotificationUnreadStatusResponse(hasUnread);
    }

    @Transactional
    public void markAsRead(Long userId, List<Long> notificationIds) {
        List<Long> distinctNotificationIds = notificationIds.stream().distinct().toList();

        if (distinctNotificationIds.isEmpty()) {
            return;
        }

        notificationRepository.markAsReadByUserIdAndIds(userId, distinctNotificationIds);
    }

    private Instant cutoff() {
        return Instant.now().minus(INBOX_RETENTION);
    }
}
