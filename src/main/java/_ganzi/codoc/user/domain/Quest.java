package _ganzi.codoc.user.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quest")
@Entity
public class Quest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "requirements", nullable = false, columnDefinition = "json")
    private String requirements;

    @Column(name = "reward", nullable = false)
    private int reward;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private QuestType type;

    @Column(name = "duration", nullable = false)
    private Integer duration;
}
