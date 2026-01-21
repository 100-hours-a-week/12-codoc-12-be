package _ganzi.codoc.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void createOnboardingUser_setsNicknameAndStatus() {
        User user = newOnboardingUser("dino");

        assertThat(user.getNickname()).isEqualTo("dino");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
    }

    @Test
    void createOnboardingUser_leavesOptionalFieldsNull() {
        User user = newOnboardingUser("dino");

        assertThat(user.getAvatar()).isNull();
        assertThat(user.getInitLevel()).isNull();
        assertThat(user.getDailyGoal()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
        assertThat(user.getLastAccess()).isNull();
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    void activate() {
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");
        user.activate();
        user2.activate();
        user3.activate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    void markDormant() {
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");
        user.markDormant();
        user2.markDormant();
        user3.markDormant();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.DORMANT);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    void reviveFromDormant() {
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");
        user.reviveFromDormant();
        user2.reviveFromDormant();
        user3.reviveFromDormant();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void touchLastAccess() {
        User user = newOnboardingUser("dino");
        assertThat(user.getLastAccess()).isNull();

        user.touchLastAccess();
        assertThat(user.getLastAccess()).isNotNull();

        Instant lastAccess = user.getLastAccess();
        user.touchLastAccess();
        assertThat(user.getLastAccess()).isNotNull();
        assertThat(user.getLastAccess()).isAfter(lastAccess);
    }

    @Test
    void updateNickname() {
        User user = newActiveUser("dino");
        user.updateNickname("ian");
        assertThat(user.getNickname()).isEqualTo("ian");
    }

    @Test
    void updateAvatar() {
        User user = newActiveUser("dino");
        Avatar avatar = newAvatar("ian", "ian");
        user.updateAvatar(avatar);
        assertThat(user.getAvatar()).isEqualTo(avatar);
    }

    @Test
    void initializeInitLevel() {
        User user = newActiveUser("dino");
        assert (user.getInitLevel() == null);
        user.initializeInitLevel(InitLevel.NEWBIE);
        assert (user.getInitLevel() == InitLevel.NEWBIE);
    }

    @Test
    void updateDailyGoal() {
        User user = newActiveUser("dino");
        user.updateDailyGoal(DailyGoal.THREE);
        assertThat(user.getDailyGoal()).isEqualTo(DailyGoal.THREE);
        user.updateDailyGoal(DailyGoal.ONE);
        assertThat(user.getDailyGoal()).isEqualTo(DailyGoal.ONE);
    }

    private User newOnboardingUser(String nickname) {
        return User.createOnboardingUser(nickname);
    }

    private User newActiveUser(String nickname) {
        User user = newOnboardingUser(nickname);
        user.activate();
        return user;
    }

    private User newDormantUser(String nickname) {
        User user = newActiveUser(nickname);
        user.markDormant();
        return user;
    }

    private Avatar newAvatar(String name, String imageUrl) {
        return Avatar.createNewAvatar(name, imageUrl);
    }
}
