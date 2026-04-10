package _ganzi.codoc.surprise.dto;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import java.time.Instant;

public record SurpriseEventSnapshot(
        Long eventId,
        Instant startsAt,
        Instant endsAt,
        int answerChoiceNo,
        int maxRewardCount) {

    public static SurpriseEventSnapshot from(SurpriseEvent event) {
        return new SurpriseEventSnapshot(
                event.getId(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getQuizPool().getAnswerChoiceNo(),
                event.getMaxRewardCount());
    }

    public boolean isOpenAt(Instant now) {
        return !now.isBefore(startsAt) && now.isBefore(endsAt);
    }
}
