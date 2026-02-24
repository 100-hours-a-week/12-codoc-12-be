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
@Table(name = "leaderboard_group_member")
@Entity
public class LeaderboardGroupMember extends BaseTimeEntity {

    @EmbeddedId private LeaderboardGroupMemberId id;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private LeaderboardGroup group;

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LeaderboardGroupMember(
            LeaderboardGroupMemberId id, LeaderboardGroup group, Integer seasonId, User user) {
        this.id = id;
        this.group = group;
        this.seasonId = seasonId;
        this.user = user;
    }

    public static LeaderboardGroupMember create(LeaderboardGroup group, Integer seasonId, User user) {
        LeaderboardGroupMemberId id = new LeaderboardGroupMemberId(group.getId(), user.getId());
        return new LeaderboardGroupMember(id, group, seasonId, user);
    }
}
