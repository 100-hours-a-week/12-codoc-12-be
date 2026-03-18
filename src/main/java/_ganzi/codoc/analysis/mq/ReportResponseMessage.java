package _ganzi.codoc.analysis.mq;

import _ganzi.codoc.analysis.dto.AnalysisReportResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ReportResponseMessage(
        @JsonProperty("job_id") String jobId,
        @JsonProperty("status") String status,
        @JsonProperty("responded_at") Instant respondedAt,
        @JsonProperty("result") AnalysisReportResponse result,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("error_message") String errorMessage) {}
