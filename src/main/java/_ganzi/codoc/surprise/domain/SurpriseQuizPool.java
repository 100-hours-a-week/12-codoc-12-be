package _ganzi.codoc.surprise.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "surprise_quiz_pool")
@Entity
public class SurpriseQuizPool extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SurpriseQuizPoolStatus status;

    @Column(name = "content", nullable = false, columnDefinition = "json")
    private String content;

    @Column(name = "answer_choice_no", nullable = false)
    private Byte answerChoiceNo;

    public void markInProgress() {
        this.status = SurpriseQuizPoolStatus.IN_PROGRESS;
    }

    public void markUsed() {
        this.status = SurpriseQuizPoolStatus.USED;
    }
}
