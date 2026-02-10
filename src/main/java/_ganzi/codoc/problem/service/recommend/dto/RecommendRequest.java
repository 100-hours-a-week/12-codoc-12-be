package _ganzi.codoc.problem.service.recommend.dto;

import _ganzi.codoc.problem.service.RecommendationScenario;
import _ganzi.codoc.problem.service.RecommendedProblemService.RecommendationFilterInfo;
import _ganzi.codoc.user.enums.InitLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendRequest(
        @JsonProperty("user_id") long userId,
        @JsonProperty("user_level") String userLevel,
        String scenario,
        @JsonProperty("filter_info") FilterInfo filterInfo) {

    public static RecommendRequest of(
            long userId,
            InitLevel userLevel,
            RecommendationScenario scenario,
            RecommendationFilterInfo filterInfo) {
        return new RecommendRequest(
                userId,
                userLevel.name(),
                scenario.name(),
                new FilterInfo(filterInfo.solvedProblemIds(), filterInfo.challengeProblemIds()));
    }

    public record FilterInfo(
            @JsonProperty("solved_problem_ids") List<Long> solvedProblemIds,
            @JsonProperty("challenge_problem_ids") List<Long> challengeProblemIds) {}
}
