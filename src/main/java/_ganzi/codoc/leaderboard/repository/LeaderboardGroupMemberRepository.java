package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardGroupMemberRepository
        extends JpaRepository<LeaderboardGroupMember, LeaderboardGroupMemberId> {}
