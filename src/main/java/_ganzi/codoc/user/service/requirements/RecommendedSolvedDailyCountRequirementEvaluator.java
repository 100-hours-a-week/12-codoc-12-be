package _ganzi.codoc.user.service.requirements;

import _ganzi.codoc.problem.repository.RecommendedProblemRepository;
import _ganzi.codoc.user.domain.UserQuest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class RecommendedSolvedDailyCountRequirementEvaluator implements QuestRequirementEvaluator {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final RecommendedProblemRepository recommendedProblemRepository;

    @Override
    public String key() {
        return "RECOMMENDED_SOLVED_DAILY_COUNT";
    }

    @Override
    public boolean isSatisfied(UserQuest userQuest, JsonNode value) {
        int required = value.asInt(0);
        if (required <= 0) {
            return false;
        }
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Instant start = today.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();
        long solvedCount =
                recommendedProblemRepository.countByUserIdAndSolvedAtBetween(
                        userQuest.getUser().getId(), start, end);
        return solvedCount >= required;
    }
}
