package _ganzi.codoc.problem.service.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendResponse(String code, String message, Data data) {

    public List<RecommendationItem> recommendations() {
        return data != null && data.recommendations() != null ? data.recommendations() : List.of();
    }

    public record Data(
            @JsonProperty("user_id") long userId,
            String version,
            List<RecommendationItem> recommendations) {}

    public record RecommendationItem(
            @JsonProperty("problem_id") Long problemId, @JsonProperty("reason_msg") String reasonMsg) {}
}
