package _ganzi.codoc.surprise.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import _ganzi.codoc.surprise.exception.SurpriseEventRewardExhaustedException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SurpriseEventTest {

    private static final int MAX_REWARD_COUNT_THREE = 3;
    private static final int MAX_REWARD_COUNT_TWO = 2;

    @Test
    void 정답_제출마다_순위가_증가한다() {
        SurpriseEvent event = newOpenEvent(MAX_REWARD_COUNT_THREE);
        Instant now = Instant.parse("2026-04-09T12:00:00Z");

        int firstRank = event.recordCorrectSubmission(now);
        int secondRank = event.recordCorrectSubmission(now.plusSeconds(1));

        assertThat(firstRank).isEqualTo(1);
        assertThat(secondRank).isEqualTo(2);
        assertThat(event.getRemainingRewardCount()).isEqualTo(1);
        assertThat(event.isRewardExhausted()).isFalse();
        assertThat(event.getStatus()).isEqualTo(SurpriseEventStatus.OPEN);
    }

    @Test
    void 최대_보상_인원에_도달하면_보상이_소진되고_이벤트가_종료된다() {
        SurpriseEvent event = newOpenEvent(MAX_REWARD_COUNT_TWO);
        Instant now = Instant.parse("2026-04-09T12:00:00Z");

        event.recordCorrectSubmission(now);
        int lastRank = event.recordCorrectSubmission(now.plusSeconds(1));

        assertThat(lastRank).isEqualTo(2);
        assertThat(event.getRemainingRewardCount()).isZero();
        assertThat(event.isRewardExhausted()).isTrue();
        assertThat(event.getRewardExhaustedAt()).isEqualTo(now.plusSeconds(1));
        assertThat(event.getStatus()).isEqualTo(SurpriseEventStatus.CLOSED);
    }

    @Test
    void 보상이_소진된_이후_정답_제출은_반영되지_않는다() {
        SurpriseEvent event = newOpenEvent(MAX_REWARD_COUNT_TWO);
        Instant now = Instant.parse("2026-04-09T12:00:00Z");

        event.recordCorrectSubmission(now);
        event.recordCorrectSubmission(now.plusSeconds(1));
        Instant rewardExhaustedAt = event.getRewardExhaustedAt();

        assertThatThrownBy(() -> event.recordCorrectSubmission(now.plusSeconds(2)))
                .isInstanceOf(SurpriseEventRewardExhaustedException.class);
        assertThat(event.getRemainingRewardCount()).isZero();
        assertThat(event.getRewardExhaustedAt()).isEqualTo(rewardExhaustedAt);
        assertThat(event.getStatus()).isEqualTo(SurpriseEventStatus.CLOSED);
    }

    private SurpriseEvent newOpenEvent(int maxRewardCount) {
        SurpriseEvent event =
                SurpriseEvent.schedule(
                        null,
                        "20260410",
                        Instant.parse("2026-04-10T11:00:00Z"),
                        Instant.parse("2026-04-10T11:50:00Z"),
                        maxRewardCount);
        event.open();
        return event;
    }
}
