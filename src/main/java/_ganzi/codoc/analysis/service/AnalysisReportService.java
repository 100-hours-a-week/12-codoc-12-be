package _ganzi.codoc.analysis.service;

import _ganzi.codoc.analysis.domain.AnalysisReport;
import _ganzi.codoc.analysis.domain.job.AnalysisReportJob;
import _ganzi.codoc.analysis.domain.job.AnalysisReportJobStatus;
import _ganzi.codoc.analysis.dto.AnalysisPeriod;
import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import _ganzi.codoc.analysis.dto.AnalysisReportResponse;
import _ganzi.codoc.analysis.infra.AnalysisReportClient;
import _ganzi.codoc.analysis.mq.ReportRequestPublisher;
import _ganzi.codoc.analysis.repository.AnalysisReportJobRepository;
import _ganzi.codoc.analysis.repository.AnalysisReportRepository;
import _ganzi.codoc.analysis.service.dto.AnalysisReportDetailResponse;
import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import _ganzi.codoc.global.exception.ResourceNotFoundException;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import _ganzi.codoc.problem.enums.ParagraphType;
import _ganzi.codoc.problem.enums.QuizType;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.repository.SummaryCardSubmissionRepository;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.submission.repository.UserQuizResultRepository;
import _ganzi.codoc.submission.service.ProblemSessionService;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AnalysisReportService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String CODE_SUCCESS = "SUCCESS";
    private static final String CODE_DEPENDENCY_NOT_READY = "DEPENDENCY_NOT_READY";
    private static final String CODE_UNAUTHORIZED_INTERNAL = "UNAUTHORIZED_INTERNAL";
    private static final String REPORT_CREATED_TITLE = "AI 분석 리포트가 도착했어요";
    private static final String REPORT_CREATED_BODY = "이번 주 학습 리포트를 확인해보세요.";

    private final AnalysisReportClient analysisReportClient;
    private final AnalysisReportRepository analysisReportRepository;
    private final UserRepository userRepository;
    private final ChatbotConversationRepository chatbotConversationRepository;
    private final SummaryCardSubmissionRepository summaryCardSubmissionRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final ProblemSessionService problemSessionService;
    private final NotificationDispatchService notificationDispatchService;
    private final ObjectMapper objectMapper;
    private final AnalysisReportJobRepository analysisReportJobRepository;
    private final ObjectProvider<ReportRequestPublisher> reportRequestPublisherProvider;

    @Value("${app.report.mq.enabled:false}")
    private boolean reportMqEnabled;

    @Value("${app.report.mq.request-publish-enabled:false}")
    private boolean reportMqRequestPublishEnabled;

    @Transactional
    public void issueWeeklyReports() {
        AnalysisWindow window = AnalysisWindow.from(ZonedDateTime.now(SEOUL));
        List<User> users = userRepository.findAllByStatus(UserStatus.ACTIVE);

        int successCount = 0;
        int publishedCount = 0;
        int dependencyNotReadyCount = 0;
        int unauthorizedCount = 0;
        int failedCount = 0;
        log.info(
                "analysis report batch start. users={}, periodStart={}, periodEnd={}",
                users.size(),
                window.periodStart(),
                window.periodEnd());

        for (User user : users) {
            Optional<AnalysisReportJob> publishedJob = publishReportJob(user, window);
            if (publishedJob.isPresent()
                    && publishedJob.get().getStatus() == AnalysisReportJobStatus.PUBLISHED) {
                publishedCount++;
                continue;
            }
            IssueResult result = issueReportForUser(user, window);
            switch (result) {
                case SUCCESS -> successCount++;
                case DEPENDENCY_NOT_READY -> dependencyNotReadyCount++;
                case UNAUTHORIZED -> unauthorizedCount++;
                case FAILED -> failedCount++;
                default -> {}
            }
        }

        log.info(
                "analysis report batch end. published={}, success={}, dependencyNotReady={},"
                        + " unauthorized={}, failed={}",
                publishedCount,
                successCount,
                dependencyNotReadyCount,
                unauthorizedCount,
                failedCount);
    }

    public AnalysisReportDetailResponse getLatestReport(Long userId) {
        AnalysisReport report =
                analysisReportRepository
                        .findFirstByUserIdOrderByPeriodEndDesc(userId)
                        .orElseThrow(ResourceNotFoundException::new);

        return new AnalysisReportDetailResponse(
                userId,
                new AnalysisPeriod(report.getPeriodStart(), report.getPeriodEnd()),
                report.getReportJson());
    }

    private IssueResult issueReportForUser(User user, AnalysisWindow window) {
        AnalysisReportRequest request = buildRequest(user, window);
        AnalysisReportResponse response = requestReportWithRetry(request, user.getId());
        return processReportResponse(user, window, response);
    }

    @Transactional
    public IssueResult applyReportResponseFromMq(
            AnalysisReportJob job, AnalysisReportResponse response) {
        User user = userRepository.findById(job.getUserId()).orElse(null);
        if (user == null) {
            return IssueResult.FAILED;
        }
        AnalysisWindow window =
                AnalysisWindow.fromPeriod(job.getPeriodStart(), job.getPeriodEnd(), SEOUL);
        return processReportResponse(user, window, response);
    }

    private Optional<AnalysisReportJob> publishReportJob(User user, AnalysisWindow window) {
        if (!reportMqEnabled || !reportMqRequestPublishEnabled) {
            return Optional.empty();
        }
        ReportRequestPublisher publisher = reportRequestPublisherProvider.getIfAvailable();
        if (publisher == null) {
            return Optional.empty();
        }

        if (analysisReportJobRepository.existsByUserIdAndPeriodStartAndPeriodEndAndStatus(
                user.getId(),
                window.periodStart(),
                window.periodEnd(),
                AnalysisReportJobStatus.PUBLISHED)) {
            return analysisReportJobRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                    user.getId(), AnalysisReportJobStatus.PUBLISHED);
        }

        String jobId = UUID.randomUUID().toString();
        Instant requestedAt = Instant.now();
        AnalysisReportJob job =
                AnalysisReportJob.publish(
                        jobId, user.getId(), window.periodStart(), window.periodEnd(), requestedAt);
        analysisReportJobRepository.save(job);

        try {
            AnalysisReportRequest request = buildRequest(user, window);
            publisher.publish(jobId, request, requestedAt);
            return Optional.of(job);
        } catch (Exception exception) {
            job.markPublishFailed("PUBLISH_FAILED", exception.getMessage());
            log.warn(
                    "analysis report publish failed. jobId={}, userId={}", jobId, user.getId(), exception);
            return Optional.of(job);
        }
    }

    private IssueResult processReportResponse(
            User user, AnalysisWindow window, AnalysisReportResponse response) {
        if (response == null || response.code() == null) {
            log.warn("analysis report response empty. userId={}", user.getId());
            return IssueResult.FAILED;
        }

        if (CODE_DEPENDENCY_NOT_READY.equals(response.code())) {
            log.info("analysis report dependency not ready. userId={}", user.getId());
            return IssueResult.DEPENDENCY_NOT_READY;
        }

        if (CODE_UNAUTHORIZED_INTERNAL.equals(response.code())) {
            log.error("analysis report unauthorized. userId={}", user.getId());
            return IssueResult.UNAUTHORIZED;
        }

        if (!CODE_SUCCESS.equals(response.code()) || response.data() == null) {
            log.warn("analysis report failed. userId={}, code={}", user.getId(), response.code());
            return IssueResult.FAILED;
        }

        String reportJson = serializeReport(response.data().report());
        AnalysisReport report =
                AnalysisReport.create(
                        user, window.periodStart(), window.periodEnd(), reportJson, Instant.now());
        analysisReportRepository.save(report);
        notificationDispatchService.dispatchAfterCommit(
                user.getId(),
                new NotificationMessageItem(
                        NotificationType.AI_ANALYSIS_REPORT_CREATED,
                        REPORT_CREATED_TITLE,
                        REPORT_CREATED_BODY,
                        Map.of(
                                "periodStart", window.periodStart().toString(),
                                "periodEnd", window.periodEnd().toString())));
        return IssueResult.SUCCESS;
    }

    private AnalysisReportResponse requestReportWithRetry(
            AnalysisReportRequest request, Long userId) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return analysisReportClient.requestReport(request);
            } catch (Exception ex) {
                log.warn(
                        "analysis report request failed. userId={}, attempt={}, error={}",
                        userId,
                        attempt,
                        ex.getClass().getSimpleName());
                if (attempt == 2) {
                    return null;
                }
            }
        }

        return null;
    }

    private AnalysisReportRequest buildRequest(User user, AnalysisWindow window) {
        List<ChatbotConversation> conversations =
                chatbotConversationRepository.findAllByUserIdAndCreatedAtBetweenWithSession(
                        user.getId(), window.startAt(), window.endAt());

        long totalChatbotRequests =
                chatbotConversationRepository.countByUserIdAndCreatedAtBetween(
                        user.getId(), window.startAt(), window.endAt());

        long solveDurationSec =
                problemSessionService.calculateSolveDurationSec(
                        user.getId(), window.startAt(), window.endAt());

        long solvedProblemsWeekly =
                userProblemResultRepository.countByUserIdAndStatusAndUpdatedAtBetween(
                        user.getId(), ProblemSolvingStatus.SOLVED, window.startAt(), window.endAt());

        Map<String, Integer> paragraphFailStats = createParagraphFailStats(user, window);
        Map<String, Integer> quizFailStats = createQuizFailStats(user, window);

        AnalysisPeriod analysisPeriod = new AnalysisPeriod(window.periodStart(), window.periodEnd());
        AnalysisReportRequest.RawMetrics rawMetrics =
                new AnalysisReportRequest.RawMetrics(
                        conversations.stream()
                                .map(conversation -> toChatbotHistory(user, conversation))
                                .toList(),
                        totalChatbotRequests,
                        solveDurationSec,
                        solvedProblemsWeekly);

        String userLevel =
                user.getInitLevel() != null ? user.getInitLevel().name().toLowerCase() : "newbie";

        return new AnalysisReportRequest(
                user.getId(), userLevel, analysisPeriod, rawMetrics, paragraphFailStats, quizFailStats);
    }

    private Map<String, Integer> createParagraphFailStats(User user, AnalysisWindow window) {
        Map<String, Integer> stats = new HashMap<>();
        for (ParagraphType type : ParagraphType.values()) {
            stats.put(type.name(), 0);
        }

        summaryCardSubmissionRepository
                .findParagraphFailCounts(user.getId(), window.startAt(), window.endAt())
                .forEach(
                        row -> stats.put(row.getParagraphType().name(), Math.toIntExact(row.getFailCount())));
        return stats;
    }

    private Map<String, Integer> createQuizFailStats(User user, AnalysisWindow window) {
        Map<String, Integer> stats = new HashMap<>();
        for (QuizType type : QuizType.values()) {
            stats.put(type.name(), 0);
        }

        userQuizResultRepository
                .findQuizFailCounts(user.getId(), window.startAt(), window.endAt())
                .forEach(row -> stats.put(row.getQuizType().name(), Math.toIntExact(row.getFailCount())));
        return stats;
    }

    private AnalysisReportRequest.ChatbotMessageHistory toChatbotHistory(
            User user, ChatbotConversation conversation) {
        return new AnalysisReportRequest.ChatbotMessageHistory(
                conversation.getId(),
                user.getId(),
                conversation.getProblemSession().getProblem().getId(),
                conversation.getAiMessage(),
                conversation.getUserMessage(),
                conversation.getParagraphType().name(),
                conversation.getCreatedAt().atZone(SEOUL).toLocalDate());
    }

    private String serializeReport(JsonNode report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize analysis report", e);
        }
    }

    private record AnalysisWindow(
            Instant startAt, Instant endAt, LocalDate periodStart, LocalDate periodEnd) {

        private static AnalysisWindow from(ZonedDateTime now) {
            ZonedDateTime endAt = now.toLocalDate().atStartOfDay(SEOUL);
            ZonedDateTime startAt = endAt.minusDays(6).plusHours(1);
            return new AnalysisWindow(
                    startAt.toInstant(), endAt.toInstant(), startAt.toLocalDate(), endAt.toLocalDate());
        }

        private static AnalysisWindow fromPeriod(
                LocalDate periodStart, LocalDate periodEnd, ZoneId zoneId) {
            ZonedDateTime startAt = periodStart.atTime(1, 0).atZone(zoneId);
            ZonedDateTime endAt = periodEnd.atStartOfDay(zoneId);
            return new AnalysisWindow(startAt.toInstant(), endAt.toInstant(), periodStart, periodEnd);
        }
    }

    public enum IssueResult {
        SUCCESS,
        DEPENDENCY_NOT_READY,
        UNAUTHORIZED,
        FAILED
    }
}
