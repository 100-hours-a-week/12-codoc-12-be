package _ganzi.codoc.leaderboard.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leaderboard_score")
@Entity
public class LeaderboardScore extends BaseTimeEntity {

    @EmbeddedId private LeaderboardScoreId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "weekly_xp", nullable = false)
    private int weeklyXp;

    private LeaderboardScore(
            LeaderboardScoreId id, User user, League league, Long groupId, int weeklyXp) {
        this.id = id;
        this.user = user;
        this.league = league;
        this.groupId = groupId;
        this.weeklyXp = weeklyXp;
    }

    public static LeaderboardScore create(
            LeaderboardScoreId id, User user, League league, Long groupId) {
        return new LeaderboardScore(id, user, league, groupId, 0);
    }

    public void addWeeklyXp(int delta) {
        this.weeklyXp += delta;
    }
}
