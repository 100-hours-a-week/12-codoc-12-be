package _ganzi.codoc.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDormantScheduler {

    private static final int DORMANT_AFTER_DAYS = 30;

    private final UserService userService;

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void markDormantUsers() {
        userService.markDormantUsersInactiveForDays(DORMANT_AFTER_DAYS);
    }
}
