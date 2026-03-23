package _ganzi.codoc.problem.event;

import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import java.time.Instant;

public record RecommendationPublishRequestedEvent(
        String jobId, RecommendRequest request, Instant requestedAt) {}
