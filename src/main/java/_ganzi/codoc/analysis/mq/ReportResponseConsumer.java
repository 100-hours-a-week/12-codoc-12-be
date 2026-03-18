package _ganzi.codoc.analysis.mq;

import _ganzi.codoc.analysis.domain.job.AnalysisReportJob;
import _ganzi.codoc.analysis.repository.AnalysisReportJobRepository;
import _ganzi.codoc.analysis.service.AnalysisReportService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.report.mq",
        name = {"enabled", "response-consume-enabled"},
        havingValue = "true")
public class ReportResponseConsumer {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final AnalysisReportJobRepository analysisReportJobRepository;
    private final AnalysisReportService analysisReportService;

    @Transactional
    @RabbitListener(
            queues = "${app.report.mq.response-queue}",
            containerFactory = "reportRabbitListenerContainerFactory")
    public void consume(ReportResponseMessage message) {
        if (message.jobId() == null || message.jobId().isBlank()) {
            log.warn("analysis report response ignored. jobId is empty");
            return;
        }

        AnalysisReportJob job = analysisReportJobRepository.findById(message.jobId()).orElse(null);
        if (job == null) {
            log.warn("analysis report response ignored. job not found. jobId={}", message.jobId());
            return;
        }
        if (job.isTerminal()) {
            log.info(
                    "analysis report response ignored. already terminal. jobId={}, status={}",
                    message.jobId(),
                    job.getStatus());
            return;
        }

        Instant respondedAt = message.respondedAt() == null ? Instant.now() : message.respondedAt();
        if (SUCCESS.equalsIgnoreCase(message.status())) {
            if (message.result() == null) {
                job.markFailed("INVALID_RESPONSE", "result is null", respondedAt);
                return;
            }
            AnalysisReportService.IssueResult result =
                    analysisReportService.applyReportResponseFromMq(job, message.result());
            if (result == AnalysisReportService.IssueResult.SUCCESS) {
                job.markDone(respondedAt);
                return;
            }
            job.markFailed(result.name(), "analysis report processing failed", respondedAt);
            return;
        }

        if (FAILED.equalsIgnoreCase(message.status())) {
            String errorCode = message.errorCode() == null ? "UNKNOWN" : message.errorCode();
            String errorMessage =
                    message.errorMessage() == null
                            ? "analysis report request failed"
                            : message.errorMessage();
            job.markFailed(errorCode, errorMessage, respondedAt);
            return;
        }

        job.markFailed("INVALID_STATUS", "unsupported status: " + message.status(), respondedAt);
    }
}
