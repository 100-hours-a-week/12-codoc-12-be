package _ganzi.codoc.notification.scheduler;

import _ganzi.codoc.notification.service.AttendanceNotificationService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AttendanceNotificationScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final AttendanceNotificationService attendanceNotificationService;

    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendDailyAttendanceReminder() {
        attendanceNotificationService.sendDailyReminder(LocalDate.now(SEOUL));
    }
}
