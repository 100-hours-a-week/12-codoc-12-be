package _ganzi.codoc.problem.domain;

import _ganzi.codoc.global.converter.StringListConverter;
import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.enums.ProblemDifficulty;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "problem")
@Entity
public class Problem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private ProblemDifficulty difficulty;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags", nullable = false, columnDefinition = "json")
    private List<String> tags;
}
