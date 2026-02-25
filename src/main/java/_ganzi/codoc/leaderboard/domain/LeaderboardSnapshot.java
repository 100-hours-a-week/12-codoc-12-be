package _ganzi.codoc.leaderboard.domain;

import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leaderboard_snapshot")
@Entity
public class LeaderboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private LeaderboardSnapshotBatch snapshotBatch;

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 10)
    private LeaderboardScopeType scopeType;

    @Column(name = "scope_id")
    private Long scopeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "weekly_xp", nullable = false)
    private int weeklyXp;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private LeaderboardSnapshot(
            LeaderboardSnapshotBatch snapshotBatch,
            Integer seasonId,
            LeaderboardScopeType scopeType,
            Long scopeId,
            User user,
            int rank,
            int weeklyXp,
            Instant updatedAt,
            Instant createdAt) {
        this.snapshotBatch = snapshotBatch;
        this.seasonId = seasonId;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.user = user;
        this.rank = rank;
        this.weeklyXp = weeklyXp;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public static LeaderboardSnapshot create(
            LeaderboardSnapshotBatch snapshotBatch,
            Integer seasonId,
            LeaderboardScopeType scopeType,
            Long scopeId,
            User user,
            int rank,
            int weeklyXp,
            Instant updatedAt,
            Instant createdAt) {
        return new LeaderboardSnapshot(
                snapshotBatch, seasonId, scopeType, scopeId, user, rank, weeklyXp, updatedAt, createdAt);
    }
}
