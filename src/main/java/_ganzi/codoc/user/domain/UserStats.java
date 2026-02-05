package _ganzi.codoc.user.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_stats")
@Entity
public class UserStats extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "solved_cnt", nullable = false)
    private int solvedCount;

    @Column(name = "solving_cnt", nullable = false)
    private int solvingCount;

    @Column(name = "streak", nullable = false)
    private int streak;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private UserStats(User user) {
        this.user = user;
    }

    public static UserStats create(User user) {
        return new UserStats(user);
    }

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

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }
}
