package _ganzi.codoc.ai.dto;

import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.domain.CustomQuiz;
import _ganzi.codoc.custom.domain.CustomSummaryCard;
import _ganzi.codoc.problem.enums.ParagraphType;
import _ganzi.codoc.problem.enums.QuizType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CustomProblemGenerationResponse(
        @JsonProperty("problem_detail") ProblemDetail problemDetail,
        @JsonProperty("summary_card") List<SummaryCardItem> summaryCard,
        List<QuizItem> quiz) {

    public record ProblemDetail(String title, String content) {}

    public record QuizItem(
            @JsonProperty("quiz_type") QuizType quizType,
            String question,
            List<String> choices,
            @JsonProperty("answer_index") int answerIndex,
            String explanation,
            int sequence) {

        public CustomQuiz toEntity(CustomProblem customProblem) {
            return CustomQuiz.create(
                    customProblem, quizType, question, explanation, choices, answerIndex, sequence);
        }
    }

    public record SummaryCardItem(
            @JsonProperty("paragraph_type") ParagraphType paragraphType,
            @JsonProperty("paragraph_order") int paragraphOrder,
            @JsonProperty("answer_index") int answerIndex,
            List<String> choices) {

        public CustomSummaryCard toEntity(CustomProblem customProblem) {
            return CustomSummaryCard.create(
                    customProblem, paragraphType, paragraphOrder, choices, answerIndex);
        }
    }
}
