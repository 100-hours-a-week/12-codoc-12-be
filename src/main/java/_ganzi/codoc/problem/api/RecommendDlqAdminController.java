package _ganzi.codoc.problem.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.problem.domain.job.RecommendDlqReprocessJob;
import _ganzi.codoc.problem.service.RecommendDlqReprocessService;
import _ganzi.codoc.problem.service.RecommendDlqReprocessService.ReprocessStatsView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recommend/dlq")
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
public class RecommendDlqAdminController {

    private final RecommendDlqReprocessService recommendDlqReprocessService;

    @PostMapping("/reprocess")
    public ResponseEntity<ApiResponse<ReprocessStartResponse>> startBatchReprocess(
            @Valid @RequestBody(required = false) ReprocessBatchRequest request) {
        ReprocessBatchRequest normalized =
                request == null ? new ReprocessBatchRequest(null, false, true) : request;
        RecommendDlqReprocessJob job =
                recommendDlqReprocessService.startBatch(
                        normalized.limit(), normalized.dryRun(), normalized.skipDead());
        return ResponseEntity.ok(
                ApiResponse.success(new ReprocessStartResponse(job.getId(), job.getStatus().name())));
    }

    @PostMapping("/reprocess/{jobId}")
    public ResponseEntity<ApiResponse<ReprocessStartResponse>> startSingleReprocess(
            @PathVariable String jobId,
            @Valid @RequestBody(required = false) ReprocessSingleRequest request) {
        ReprocessSingleRequest normalized =
                request == null ? new ReprocessSingleRequest(false, true) : request;
        RecommendDlqReprocessJob job =
                recommendDlqReprocessService.startSingle(jobId, normalized.dryRun(), normalized.skipDead());
        return ResponseEntity.ok(
                ApiResponse.success(new ReprocessStartResponse(job.getId(), job.getStatus().name())));
    }

    @GetMapping("/reprocess-jobs/{id}")
    public ResponseEntity<ApiResponse<ReprocessStatsView>> getReprocessJob(@PathVariable Long id) {
        RecommendDlqReprocessJob job = recommendDlqReprocessService.getJob(id);
        return ResponseEntity.ok(ApiResponse.success(ReprocessStatsView.from(job)));
    }

    public record ReprocessBatchRequest(
            @Min(1) @Max(100) Integer limit, boolean dryRun, boolean skipDead) {}

    public record ReprocessSingleRequest(boolean dryRun, boolean skipDead) {}

    public record ReprocessStartResponse(Long reprocessJobId, String status) {}
}
