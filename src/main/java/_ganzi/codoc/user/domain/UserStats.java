package _ganzi.codoc.user.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_stats")
@Entity
public class UserStats extends BaseTimeEntity {

    @Id private Long id;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "solved_cnt", nullable = false)
    private int solvedCount;

    @Column(name = "solving_cnt", nullable = false)
    private int solvingCount;

    @Column(name = "streak", nullable = false)
    private int streak;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;

    public void addXp(int amount) {
        this.xp += amount;
    }

    public void increaseSolvingCount() {
        this.solvingCount++;
    }

    public void decreaseSolvingCount() {
        this.solvingCount--;
    }

    public void increaseSolvedCount() {
        this.solvedCount++;
    }

    public void increaseStreak() {
        this.streak++;
    }

    public void resetStreak() {
        this.streak = 0;
    }

    public void applyProblemSolved(int xpAmount) {
        addXp(xpAmount);
        increaseSolvedCount();
        decreaseSolvingCount();
    }
}
