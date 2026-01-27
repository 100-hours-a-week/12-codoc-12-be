package _ganzi.codoc.user.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_solved_count",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "date"})})
@Entity
public class DailySolvedCount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "solved_count", nullable = false)
    private int solvedCount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    private DailySolvedCount(User user, LocalDate date, int solvedCount) {
        this.user = user;
        this.date = date;
        this.solvedCount = solvedCount;
    }

    public static DailySolvedCount create(User user, LocalDate date) {
        return new DailySolvedCount(user, date, 0);
    }

    public void increaseSolvedCount() {
        this.solvedCount++;
    }
}
