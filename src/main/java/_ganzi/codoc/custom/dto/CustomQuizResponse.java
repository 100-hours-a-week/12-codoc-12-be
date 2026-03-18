package _ganzi.codoc.custom.dto;

import _ganzi.codoc.custom.domain.CustomQuiz;
import _ganzi.codoc.problem.enums.QuizType;
import java.util.List;

public record CustomQuizResponse(
        Long quizId,
        QuizType quizType,
        String question,
        String explanation,
        List<String> choices,
        int answerIndex,
        int sequence) {

    public static CustomQuizResponse from(CustomQuiz quiz) {
        return new CustomQuizResponse(
                quiz.getId(),
                quiz.getQuizType(),
                quiz.getQuestion(),
                quiz.getExplanation(),
                quiz.getChoices(),
                quiz.getAnswerIndex(),
                quiz.getSequence());
    }
}
