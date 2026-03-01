package _ganzi.codoc.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AnalysisReportRequest(
        @JsonProperty("user_id") long userId,
        @JsonProperty("user_level") String userLevel,
        @JsonProperty("analysis_period") AnalysisPeriod analysisPeriod,
        @JsonProperty("raw_metrics") RawMetrics rawMetrics,
        @JsonProperty("paragraph_fail_stats") Map<String, Integer> paragraphFailStats,
        @JsonProperty("quiz_fail_stats") Map<String, Integer> quizFailStats) {

    public record RawMetrics(
            @JsonProperty("chatbot_msg_history") List<ChatbotMessageHistory> chatbotMsgHistory,
            @JsonProperty("total_chatbot_requests") long totalChatbotRequests,
            @JsonProperty("solve_duration_sec") long solveDurationSec,
            @JsonProperty("solved_problems_weekly") long solvedProblemsWeekly) {}

    public record ChatbotMessageHistory(
            long id,
            @JsonProperty("user_id") long userId,
            @JsonProperty("problem_id") long problemId,
            @JsonProperty("ai_message") String aiMessage,
            @JsonProperty("user_message") String userMessage,
            String node,
            @JsonProperty("send_at") LocalDate sendAt) {}
}
