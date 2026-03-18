package _ganzi.codoc.analysis.mq;

import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ReportRequestMessage(
        @JsonProperty("job_id") String jobId,
        @JsonProperty("requested_at") Instant requestedAt,
        @JsonProperty("payload") AnalysisReportRequest payload) {}
