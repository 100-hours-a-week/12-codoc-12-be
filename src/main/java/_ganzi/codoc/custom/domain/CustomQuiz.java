package _ganzi.codoc.custom.domain;

import _ganzi.codoc.global.converter.StringListConverter;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.enums.QuizType;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "custom_quiz")
@Entity
public class CustomQuiz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "custom_problem_id", nullable = false)
    private CustomProblem customProblem;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false, length = 30)
    private QuizType quizType;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "explanation", nullable = false, length = 2000)
    private String explanation;

    @Convert(converter = StringListConverter.class)
    @Column(name = "choices", nullable = false, columnDefinition = "json")
    private List<String> choices;

    @Column(name = "answer_index", nullable = false)
    private int answerIndex;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    private CustomQuiz(
            CustomProblem customProblem,
            QuizType quizType,
            String question,
            String explanation,
            List<String> choices,
            int answerIndex,
            int sequence) {
        this.customProblem = customProblem;
        this.quizType = quizType;
        this.question = question;
        this.explanation = explanation;
        this.choices = List.copyOf(choices);
        this.answerIndex = answerIndex;
        this.sequence = sequence;
    }

    public static CustomQuiz create(
            CustomProblem customProblem,
            QuizType quizType,
            String question,
            String explanation,
            List<String> choices,
            int answerIndex,
            int sequence) {
        return new CustomQuiz(
                customProblem, quizType, question, explanation, choices, answerIndex, sequence);
    }
}
