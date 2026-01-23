package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Quiz;
import java.util.List;
import lombok.Builder;

@Builder
public record QuizResponse(Long quizId, String question, List<String> choices) {

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .quizId(quiz.getId())
                .question(quiz.getQuestion())
                .choices(quiz.getChoices())
                .build();
    }
}
