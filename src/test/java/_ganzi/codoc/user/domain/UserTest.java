package _ganzi.codoc.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 온보딩_유저를_생성하면_닉네임과_상태가_설정된다() {
        // given
        User user = newOnboardingUser("dino");

        // then
        assertThat(user.getNickname()).isEqualTo("dino");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
    }

    @Test
    void 온보딩_유저를_생성하면_선택_필드는_null이다() {
        // given
        User user = newOnboardingUser("dino");

        // then
        assertThat(user.getAvatar()).isNull();
        assertThat(user.getInitLevel()).isNull();
        assertThat(user.getDailyGoal()).isNull();
    }

    @Test
    void 온보딩_완료_시_상태와_초기수준과_일일목표가_설정된다() {
        // given
        User user = newOnboardingUser("dino");
        User user2 = newOnboardingUser("ian");

        // when
        user2.completeOnboarding(InitLevel.SPECIALIST, DailyGoal.FIVE);

        // then
        assertThat(user.getDailyGoal()).isNull();
        assertThat(user.getInitLevel()).isNull();
        assertThat(user2.getInitLevel()).isEqualTo(InitLevel.SPECIALIST);
        assertThat(user2.getDailyGoal()).isEqualTo(DailyGoal.FIVE);
    }

    @Test
    void 온보딩_완료는_온보딩_상태의_유저에게_만_적용된다() {
        // given
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");

        // when
        user.completeOnboarding(InitLevel.NEWBIE, DailyGoal.THREE);
        user2.completeOnboarding(InitLevel.NEWBIE, DailyGoal.THREE);
        user3.completeOnboarding(InitLevel.NEWBIE, DailyGoal.THREE);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    void 휴먼_처리는_활성_상태_유저에게_만_적용된다() {
        // given
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");

        // when
        user.markDormant();
        user2.markDormant();
        user3.markDormant();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.DORMANT);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    void 휴면_복귀_시_상태_전이가_적용된다() {
        // given
        User user = newOnboardingUser("dino");
        User user2 = newActiveUser("ian");
        User user3 = newDormantUser("codoc");

        // when
        user.reviveFromDormant();
        user2.reviveFromDormant();
        user3.reviveFromDormant();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONBOARDING);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 마지막_접속_시간을_갱신하면_시간이_업데이트된다() {
        // given
        User user = newOnboardingUser("dino");

        // when
        user.touchLastAccess();
        Instant lastAccess = user.getLastAccess();
        user.touchLastAccess();

        // then
        assertThat(user.getLastAccess()).isNotNull();
        assertThat(user.getLastAccess()).isAfter(lastAccess);
    }

    @Test
    void 닉네임을_변경하면_닉네임이_갱신된다() {
        // given
        User user = newActiveUser("dino");

        // when
        user.updateNickname("ian");

        // then
        assertThat(user.getNickname()).isEqualTo("ian");
    }

    @Test
    void 아바타를_변경하면_아바타가_갱신된다() {
        // given
        User user = newActiveUser("dino");
        Avatar avatar = newAvatar("ian", "ian");

        // when
        user.updateAvatar(avatar);

        // then
        assertThat(user.getAvatar()).isEqualTo(avatar);
    }

    @Test
    void 일일_목표를_변경하면_목표가_갱신된다() {
        // given
        User user = newActiveUser("dino");

        // when
        user.updateDailyGoal(DailyGoal.ONE);

        // then
        assertThat(user.getDailyGoal()).isEqualTo(DailyGoal.ONE);
    }

    private User newOnboardingUser(String nickname) {
        return User.createOnboardingUser(nickname);
    }

    private User newActiveUser(String nickname) {
        User user = newOnboardingUser(nickname);
        user.completeOnboarding(InitLevel.NEWBIE, DailyGoal.THREE);
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
