package _ganzi.codoc.leaderboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class LeaderboardScoreId implements Serializable {

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public LeaderboardScoreId(Integer seasonId, Long userId) {
        this.seasonId = seasonId;
        this.userId = userId;
    }
}
