package _ganzi.codoc.leaderboard.domain;

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
@Table(name = "leaderboard_season")
@Entity
public class LeaderboardSeason extends BaseTimeEntity {

    @Id
    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    private LeaderboardSeason(Integer seasonId, Instant startsAt, Instant endsAt) {
        this.seasonId = seasonId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public static LeaderboardSeason create(Integer seasonId, Instant startsAt, Instant endsAt) {
        return new LeaderboardSeason(seasonId, startsAt, endsAt);
    }
}
