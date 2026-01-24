package _ganzi.codoc.problem.domain;

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
@Table(name = "summary_card")
@Entity
public class SummaryCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "paragraph_type", nullable = false)
    private ParagraphType paragraphType;

    @Column(name = "paragraph_order", nullable = false)
    private int paragraphOrder;

    @Convert(converter = StringListConverter.class)
    @Column(name = "choices", nullable = false, columnDefinition = "json")
    private List<String> choices;

    @Column(name = "answer_index", nullable = false)
    private int answerIndex;
}
