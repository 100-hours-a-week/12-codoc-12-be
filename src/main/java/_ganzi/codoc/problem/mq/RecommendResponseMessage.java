package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record RecommendResponseMessage(
        @JsonProperty("job_id") String jobId,
        @JsonProperty("status") String status,
        @JsonProperty("responded_at") Instant respondedAt,
        @JsonProperty("result") RecommendResponse result,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("error_message") String errorMessage) {}
