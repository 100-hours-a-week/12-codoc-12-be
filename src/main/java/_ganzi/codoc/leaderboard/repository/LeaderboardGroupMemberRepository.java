package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMemberId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardGroupMemberRepository
        extends JpaRepository<LeaderboardGroupMember, LeaderboardGroupMemberId> {

    Optional<LeaderboardGroupMember> findFirstBySeasonIdAndUserId(Integer seasonId, Long userId);
}
