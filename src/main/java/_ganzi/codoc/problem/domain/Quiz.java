package _ganzi.codoc.problem.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_tag_id", nullable = false)
    private QuizTag quizTag;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "choice", nullable = false, columnDefinition = "json")
    private String choice;

    @Column(name = "answer", columnDefinition = "json")
    private String answer;

    @Column(name = "sequence", nullable = false)
    private int sequence;
}
