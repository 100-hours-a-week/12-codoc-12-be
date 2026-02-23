package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AttendanceNotificationService {

    private static final String title = "연속 학습이 깨질 수 있어요";
    private static final String body = "문제 하나만 풀면 연속 학습을 유지할 수 있어요.";

    private final UserRepository userRepository;
    private final NotificationSendService notificationSendService;

    public void sendDailyReminder(LocalDate targetDate) {
        List<Long> targetUserIds = userRepository.findActiveUserIdsWithoutSolvedCountOn(targetDate);

        NotificationMessageItem messageItem =
                new NotificationMessageItem(NotificationType.ATTENDANCE, title, body, null);

        for (Long userId : targetUserIds) {
            notificationSendService.send(userId, messageItem);
        }
    }
}
