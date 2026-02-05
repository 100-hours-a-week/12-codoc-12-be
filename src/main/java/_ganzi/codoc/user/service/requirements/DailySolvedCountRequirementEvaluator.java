package _ganzi.codoc.user.service.requirements;

import _ganzi.codoc.user.domain.DailySolvedCount;
import _ganzi.codoc.user.domain.UserQuest;
import _ganzi.codoc.user.repository.DailySolvedCountRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class DailySolvedCountRequirementEvaluator implements QuestRequirementEvaluator {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final DailySolvedCountRepository dailySolvedCountRepository;

    @Override
    public String key() {
        return "DailySolvedCount";
    }

    @Override
    public boolean isSatisfied(UserQuest userQuest, JsonNode value) {
        int required = value.asInt(0);
        if (required <= 0) {
            return false;
        }
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        int solvedCount =
                dailySolvedCountRepository
                        .findByUserAndDate(userQuest.getUser(), today)
                        .map(DailySolvedCount::getSolvedCount)
                        .orElse(0);
        return solvedCount >= required;
    }
}
