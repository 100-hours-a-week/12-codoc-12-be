package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record RecommendRequestMessage(
        @JsonProperty("job_id") String jobId,
        @JsonProperty("requested_at") Instant requestedAt,
        @JsonProperty("payload") RecommendRequest payload) {}
