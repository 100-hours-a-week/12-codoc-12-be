package _ganzi.codoc.custom.domain;

import _ganzi.codoc.global.converter.StringListConverter;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.enums.ParagraphType;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "custom_summary_card")
@Entity
public class CustomSummaryCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "custom_problem_id", nullable = false)
    private CustomProblem customProblem;

    @Enumerated(EnumType.STRING)
    @Column(name = "paragraph_type", nullable = false, length = 30)
    private ParagraphType paragraphType;

    @Column(name = "paragraph_order", nullable = false)
    private int paragraphOrder;

    @Convert(converter = StringListConverter.class)
    @Column(name = "choices", nullable = false, columnDefinition = "json")
    private List<String> choices;

    @Column(name = "answer_index", nullable = false)
    private int answerIndex;

    private CustomSummaryCard(
            CustomProblem customProblem,
            ParagraphType paragraphType,
            int paragraphOrder,
            List<String> choices,
            int answerIndex) {
        this.customProblem = customProblem;
        this.paragraphType = paragraphType;
        this.paragraphOrder = paragraphOrder;
        this.choices = List.copyOf(choices);
        this.answerIndex = answerIndex;
    }

    public static CustomSummaryCard create(
            CustomProblem customProblem,
            ParagraphType paragraphType,
            int paragraphOrder,
            List<String> choices,
            int answerIndex) {
        return new CustomSummaryCard(
                customProblem, paragraphType, paragraphOrder, choices, answerIndex);
    }
}
