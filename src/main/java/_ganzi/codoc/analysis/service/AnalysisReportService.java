package _ganzi.codoc.analysis.service;

import _ganzi.codoc.analysis.domain.AnalysisReport;
import _ganzi.codoc.analysis.dto.AnalysisPeriod;
import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import _ganzi.codoc.analysis.dto.AnalysisReportResponse;
import _ganzi.codoc.analysis.infra.AnalysisReportClient;
import _ganzi.codoc.analysis.repository.AnalysisReportRepository;
import _ganzi.codoc.analysis.service.dto.AnalysisReportDetailResponse;
import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import _ganzi.codoc.global.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final AnalysisReportClient analysisReportClient;
    private final AnalysisReportRepository analysisReportRepository;
    private final UserRepository userRepository;
    private final ChatbotConversationRepository chatbotConversationRepository;
    private final SummaryCardSubmissionRepository summaryCardSubmissionRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final ProblemSessionService problemSessionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void issueWeeklyReports() {
        AnalysisWindow window = AnalysisWindow.from(ZonedDateTime.now(SEOUL));
        List<User> users = userRepository.findAllByStatus(UserStatus.ACTIVE);

        for (User user : users) {
            issueReportForUser(user, window);
        }
    }

    public AnalysisReportDetailResponse getLatestReport(Long userId) {
        AnalysisReport report =
                analysisReportRepository
                        .findFirstByUserIdOrderByPeriodEndDesc(userId)
                        .orElseThrow(ResourceNotFoundException::new);

        JsonNode reportNode = parseReportJson(report.getReportJson());
        return new AnalysisReportDetailResponse(
                report.getPeriodStart(), report.getPeriodEnd(), reportNode);
    }

    private void issueReportForUser(User user, AnalysisWindow window) {
        AnalysisReportRequest request = buildRequest(user, window);
        AnalysisReportResponse response = analysisReportClient.requestReport(request);

        if (response == null || response.code() == null) {
            log.warn("analysis report response empty. userId={}", user.getId());
            return;
        }

        if (CODE_DEPENDENCY_NOT_READY.equals(response.code())) {
            log.info("analysis report dependency not ready. userId={}", user.getId());
            return;
        }

        if (CODE_UNAUTHORIZED_INTERNAL.equals(response.code())) {
            log.error("analysis report unauthorized. userId={}", user.getId());
            throw new IllegalStateException("Analysis report unauthorized");
        }

        if (!CODE_SUCCESS.equals(response.code()) || response.data() == null) {
            log.warn("analysis report failed. userId={}, code={}", user.getId(), response.code());
            return;
        }

        String reportJson = serializeReport(response.data().report());
        AnalysisReport report =
                AnalysisReport.create(
                        user, window.periodStart(), window.periodEnd(), reportJson, Instant.now());
        analysisReportRepository.save(report);
    }

    private AnalysisReportRequest buildRequest(User user, AnalysisWindow window) {
        List<ChatbotConversation> conversations =
                chatbotConversationRepository.findAllByUserIdAndCreatedAtBetweenWithAttempt(
                        user.getId(), window.startAt(), window.endAt());

        long totalChatbotRequests =
                chatbotConversationRepository.countByUserIdAndCreatedAtBetween(
                        user.getId(), window.startAt(), window.endAt());

        long solveDurationSec =
                problemSessionService.calculateSolveDurationSec(
                        user.getId(), window.startAt(), window.endAt());

        long solvedProblemWeekly =
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
                        solvedProblemWeekly);

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
                conversation.getAttempt().getProblem().getId(),
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

    private JsonNode parseReportJson(String reportJson) {
        try {
            return objectMapper.readTree(reportJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse analysis report", e);
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
    }
}
