package _ganzi.codoc.auth.service;

import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.DailyGoal;
import _ganzi.codoc.user.enums.InitLevel;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevAuthService {

    private static final InitLevel DEFAULT_INIT_LEVEL = InitLevel.NEWBIE;
    private static final DailyGoal DEFAULT_DAILY_GOAL = DailyGoal.ONE;

    private final UserService userService;

    @Transactional
    public User createActiveUser(String nickname) {
        User user = userService.createOnboardingUser();
        if (nickname != null && !nickname.isBlank()) {
            userService.updateProfile(user.getId(), nickname, null);
        }
        userService.completeOnboarding(
                user.getId(), new UserInitSurveyRequest(DEFAULT_INIT_LEVEL, DEFAULT_DAILY_GOAL));
        return userService.getUser(user.getId());
    }

    @Transactional
    public User getActiveUser(Long userId) {
        User user = userService.getUser(userId);
        if (user.getStatus() == UserStatus.DELETED) {
            user.restoreActiveFromDeleted();
        }
        if (user.getStatus() == UserStatus.DORMANT) {
            user.reviveFromDormant();
        }
        if (user.getStatus() == UserStatus.ONBOARDING) {
            userService.completeOnboarding(
                    user.getId(), new UserInitSurveyRequest(DEFAULT_INIT_LEVEL, DEFAULT_DAILY_GOAL));
        }
        return user;
    }
}
