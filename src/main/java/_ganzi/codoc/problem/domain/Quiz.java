package _ganzi.codoc.problem.domain;

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
@Table(name = "quiz")
@Entity
public class Quiz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    private QuizType quizType;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "explanation", nullable = false, length = 500)
    private String explanation;

    @Convert(converter = StringListConverter.class)
    @Column(name = "choices", nullable = false, columnDefinition = "json")
    private List<String> choices;

    @Column(name = "answer_index", nullable = false)
    private int answerIndex;

    @Column(name = "sequence", nullable = false)
    private int sequence;
}
