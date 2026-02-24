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
public class LeaderboardGroupMemberId implements Serializable {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public LeaderboardGroupMemberId(Long groupId, Long userId) {
        this.groupId = groupId;
        this.userId = userId;
    }
}
