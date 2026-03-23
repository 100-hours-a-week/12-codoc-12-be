package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.alert.DiscordAlertService;
import _ganzi.codoc.global.exception.ConflictException;
import _ganzi.codoc.global.exception.ResourceNotFoundException;
import _ganzi.codoc.problem.config.RecommendDlqProperties;
import _ganzi.codoc.problem.config.RecommendMqProperties;
import _ganzi.codoc.problem.domain.job.RecommendDlqReprocessJob;
import _ganzi.codoc.problem.domain.job.RecommendDlqReprocessJobStatus;
import _ganzi.codoc.problem.mq.RecommendResponseMessage;
import _ganzi.codoc.problem.repository.job.RecommendDlqReprocessJobRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
public class RecommendDlqReprocessService {

    private static final double ALERT_FAILURE_RATE_THRESHOLD = 0.1d;

    private final RecommendDlqReprocessJobRepository reprocessJobRepository;
    private final RecommendDlqProperties recommendDlqProperties;
    private final RecommendMqProperties recommendMqProperties;

    @Qualifier("recommendRabbitTemplate")
    private final RabbitTemplate recommendRabbitTemplate;

    private final RecommendResponseProcessingService recommendResponseProcessingService;
    private final TaskExecutor taskExecutor;
    private final DiscordAlertService discordAlertService;
    private final MessageConverter recommendMessageConverter;

    public RecommendDlqReprocessService(
            RecommendDlqReprocessJobRepository reprocessJobRepository,
            RecommendDlqProperties recommendDlqProperties,
            RecommendMqProperties recommendMqProperties,
            @Qualifier("recommendRabbitTemplate") RabbitTemplate recommendRabbitTemplate,
            RecommendResponseProcessingService recommendResponseProcessingService,
            @Qualifier("recommendDlqTaskExecutor") TaskExecutor taskExecutor,
            DiscordAlertService discordAlertService,
            @Qualifier("recommendMessageConverter") MessageConverter recommendMessageConverter) {
        this.reprocessJobRepository = reprocessJobRepository;
        this.recommendDlqProperties = recommendDlqProperties;
        this.recommendMqProperties = recommendMqProperties;
        this.recommendRabbitTemplate = recommendRabbitTemplate;
        this.recommendResponseProcessingService = recommendResponseProcessingService;
        this.taskExecutor = taskExecutor;
        this.discordAlertService = discordAlertService;
        this.recommendMessageConverter = recommendMessageConverter;
    }

    @Transactional
    public RecommendDlqReprocessJob startBatch(
            Integer requestedLimit, boolean dryRun, boolean skipDead) {
        reprocessJobRepository
                .findLatestRunningJob()
                .ifPresent(
                        existing -> {
                            throw new ConflictException();
                        });

        int limit = normalizeLimit(requestedLimit);
        RecommendDlqReprocessJob job =
                reprocessJobRepository.save(RecommendDlqReprocessJob.createBatch(limit));
        taskExecutor.execute(() -> executeBatch(job.getId(), limit, dryRun, skipDead));
        return job;
    }

    @Transactional
    public RecommendDlqReprocessJob startSingle(
            String targetJobId, boolean dryRun, boolean skipDead) {
        reprocessJobRepository
                .findLatestRunningJob()
                .ifPresent(
                        existing -> {
                            throw new ConflictException();
                        });

        RecommendDlqReprocessJob job =
                reprocessJobRepository.save(RecommendDlqReprocessJob.createSingle(targetJobId));
        taskExecutor.execute(
                () ->
                        executeSingle(
                                job.getId(),
                                targetJobId,
                                Math.max(1, recommendDlqProperties.reprocessSingleScanLimit()),
                                dryRun,
                                skipDead));
        return job;
    }

    @Transactional(readOnly = true)
    public RecommendDlqReprocessJob getJob(Long jobId) {
        return reprocessJobRepository.findById(jobId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeBatch(Long reprocessJobId, int limit, boolean dryRun, boolean skipDead) {
        ReprocessStats stats = new ReprocessStats();
        startJob(reprocessJobId);
        try {
            for (int i = 0; i < limit; i++) {
                Message raw = recommendRabbitTemplate.receive(recommendMqProperties.responseDlq());
                if (raw == null) {
                    break;
                }
                processOne(raw, dryRun, skipDead, stats);
            }
            finishJobSuccess(reprocessJobId, stats);
            alertReprocessSummary("BATCH", reprocessJobId, stats);
        } catch (Exception exception) {
            log.error("recommend dlq batch reprocess failed. jobId={}", reprocessJobId, exception);
            finishJobFailed(reprocessJobId, stats, exception);
            alertReprocessSummary("BATCH_FAILED", reprocessJobId, stats);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeSingle(
            Long reprocessJobId, String targetJobId, int scanLimit, boolean dryRun, boolean skipDead) {
        ReprocessStats stats = new ReprocessStats();
        startJob(reprocessJobId);

        List<Message> retained = new ArrayList<>();
        try {
            Message target = null;
            for (int i = 0; i < scanLimit; i++) {
                Message raw = recommendRabbitTemplate.receive(recommendMqProperties.responseDlq());
                if (raw == null) {
                    break;
                }
                String jobId = extractJobId(raw);
                if (target == null && targetJobId.equals(jobId)) {
                    target = raw;
                    continue;
                }
                retained.add(raw);
            }

            for (Message raw : retained) {
                recommendRabbitTemplate.send("", recommendMqProperties.responseDlq(), raw);
            }

            if (target == null) {
                stats.skippedCount += 1;
                finishJobSuccess(reprocessJobId, stats);
                alertReprocessSummary("SINGLE_NOT_FOUND", reprocessJobId, stats);
                return;
            }

            processOne(target, dryRun, skipDead, stats);
            finishJobSuccess(reprocessJobId, stats);
            alertReprocessSummary("SINGLE", reprocessJobId, stats);
        } catch (Exception exception) {
            log.error("recommend dlq single reprocess failed. jobId={}", reprocessJobId, exception);
            finishJobFailed(reprocessJobId, stats, exception);
            alertReprocessSummary("SINGLE_FAILED", reprocessJobId, stats);
        }
    }

    private void processOne(Message raw, boolean dryRun, boolean skipDead, ReprocessStats stats) {
        RecommendResponseMessage message;
        try {
            Object converted = recommendMessageConverter.fromMessage(raw);
            message = (RecommendResponseMessage) converted;
        } catch (MessageConversionException | ClassCastException exception) {
            stats.fail(extractJobId(raw));
            requeueToDlq(raw);
            return;
        }

        if (message == null || message.jobId() == null || message.jobId().isBlank()) {
            stats.fail(extractJobId(raw));
            requeueToDlq(raw);
            return;
        }

        if (dryRun) {
            stats.skippedCount += 1;
            requeueToDlq(raw);
            return;
        }

        try {
            RecommendResponseProcessingService.RecommendResponseProcessResult result =
                    recommendResponseProcessingService.process(message);
            if (result.isSkipped()) {
                stats.skippedCount += 1;
                if (!skipDead) {
                    requeueToDlq(raw);
                }
                return;
            }
            stats.successCount += 1;
        } catch (Exception exception) {
            stats.fail(message.jobId());
            requeueToDlq(raw);
            log.warn("recommend dlq reprocess failed. messageJobId={}", message.jobId(), exception);
        }
    }

    private String extractJobId(Message raw) {
        if (raw == null || raw.getMessageProperties() == null) {
            return null;
        }
        Object jobIdHeader = raw.getMessageProperties().getHeaders().get("jobId");
        if (jobIdHeader instanceof String value && !value.isBlank()) {
            return value;
        }
        if (raw.getMessageProperties().getMessageId() != null
                && !raw.getMessageProperties().getMessageId().isBlank()) {
            return raw.getMessageProperties().getMessageId();
        }
        return null;
    }

    private void requeueToDlq(Message raw) {
        recommendRabbitTemplate.send("", recommendMqProperties.responseDlq(), raw);
    }

    private int normalizeLimit(Integer requestedLimit) {
        int defaultLimit = Math.max(1, recommendDlqProperties.reprocessDefaultLimit());
        int maxLimit = Math.max(defaultLimit, recommendDlqProperties.reprocessMaxLimit());
        int actual = requestedLimit == null ? defaultLimit : requestedLimit;
        return Math.min(Math.max(1, actual), maxLimit);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startJob(Long reprocessJobId) {
        reprocessJobRepository
                .findById(reprocessJobId)
                .ifPresent(
                        job -> {
                            job.start(Instant.now());
                            reprocessJobRepository.save(job);
                        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishJobSuccess(Long reprocessJobId, ReprocessStats stats) {
        String failedJobIdsJson = serializeFailedJobIds(stats.failedJobIds);
        reprocessJobRepository
                .findById(reprocessJobId)
                .ifPresent(
                        job -> {
                            job.finishSuccess(
                                    stats.successCount,
                                    stats.failedCount,
                                    stats.skippedCount,
                                    failedJobIdsJson,
                                    Instant.now());
                            reprocessJobRepository.save(job);
                        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishJobFailed(Long reprocessJobId, ReprocessStats stats, Exception exception) {
        String failedJobIdsJson = serializeFailedJobIds(stats.failedJobIds);
        reprocessJobRepository
                .findById(reprocessJobId)
                .ifPresent(
                        job -> {
                            job.finishFailed(
                                    stats.successCount,
                                    stats.failedCount,
                                    stats.skippedCount,
                                    failedJobIdsJson,
                                    exception.getMessage(),
                                    Instant.now());
                            reprocessJobRepository.save(job);
                        });
    }

    private String serializeFailedJobIds(List<String> failedJobIds) {
        if (failedJobIds == null || failedJobIds.isEmpty()) {
            return "[]";
        }
        return "[\""
                + failedJobIds.stream()
                        .map(this::escapeJson)
                        .reduce((left, right) -> left + "\",\"" + right)
                        .orElse("")
                + "\"]";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "UNKNOWN";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void alertReprocessSummary(String mode, Long reprocessJobId, ReprocessStats stats) {
        int total = stats.successCount + stats.failedCount + stats.skippedCount;
        if (total <= 0) {
            return;
        }
        String summary =
                String.format(
                        "[recommend-dlq] reprocess %s completed. reprocessJobId=%d success=%d failed=%d"
                                + " skipped=%d",
                        mode, reprocessJobId, stats.successCount, stats.failedCount, stats.skippedCount);
        discordAlertService.send(summary);

        if (stats.failedCount <= 0) {
            return;
        }
        double failureRate = (double) stats.failedCount / (double) total;
        if (failureRate >= ALERT_FAILURE_RATE_THRESHOLD) {
            discordAlertService.send(
                    String.format(
                            "[recommend-dlq] reprocess failure rate high. reprocessJobId=%d "
                                    + "failureRate=%.2f%% threshold=%.2f%%",
                            reprocessJobId, failureRate * 100.0, ALERT_FAILURE_RATE_THRESHOLD * 100.0));
        }
    }

    public record ReprocessStatsView(
            Long id,
            RecommendDlqReprocessJobStatus status,
            int requestedLimit,
            int successCount,
            int failedCount,
            int skippedCount,
            String failedJobIdsJson,
            String lastError,
            Instant startedAt,
            Instant finishedAt,
            Instant createdAt) {

        public static ReprocessStatsView from(RecommendDlqReprocessJob job) {
            return new ReprocessStatsView(
                    job.getId(),
                    job.getStatus(),
                    job.getRequestedLimit(),
                    job.getSuccessCount(),
                    job.getFailedCount(),
                    job.getSkippedCount(),
                    job.getFailedJobIdsJson(),
                    job.getLastError(),
                    job.getStartedAt(),
                    job.getFinishedAt(),
                    job.getCreatedAt());
        }
    }

    private static class ReprocessStats {
        private int successCount;
        private int failedCount;
        private int skippedCount;
        private final List<String> failedJobIds = new ArrayList<>();

        private void fail(String jobId) {
            this.failedCount += 1;
            this.failedJobIds.add(jobId == null ? "UNKNOWN" : jobId);
        }
    }
}
