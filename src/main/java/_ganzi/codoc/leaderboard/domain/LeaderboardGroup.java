package _ganzi.codoc.leaderboard.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leaderboard_group")
@Entity
public class LeaderboardGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private LeaderboardSeason season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "group_no", nullable = false)
    private int groupNo;

    private LeaderboardGroup(LeaderboardSeason season, League league, int groupNo) {
        this.season = season;
        this.league = league;
        this.groupNo = groupNo;
    }

    public static LeaderboardGroup create(LeaderboardSeason season, League league, int groupNo) {
        return new LeaderboardGroup(season, league, groupNo);
    }
}
