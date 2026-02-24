package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardScoreId;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupMemberRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardScoreRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardScoreService {

    private final LeaderboardSeasonRepository seasonRepository;
    private final LeaderboardScoreRepository scoreRepository;
    private final LeaderboardGroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addWeeklyXp(Long userId, int delta) {
        if (delta <= 0) {
            return;
        }
        LeaderboardSeason season = findCurrentSeason().orElse(null);
        if (season == null) {
            return;
        }
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getLeague() == null) {
            return;
        }
        LeaderboardScoreId scoreId = new LeaderboardScoreId(season.getSeasonId(), userId);
        LeaderboardScore score =
                scoreRepository
                        .findById(scoreId)
                        .orElseGet(() -> scoreRepository.save(createScore(scoreId, season, user)));
        score.addWeeklyXp(delta);
    }

    private Optional<LeaderboardSeason> findCurrentSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
                now, now);
    }

    private LeaderboardScore createScore(
            LeaderboardScoreId scoreId, LeaderboardSeason season, User user) {
        Long groupId = resolveGroupId(season.getSeasonId(), user.getId()).orElse(null);
        return LeaderboardScore.create(scoreId, user, user.getLeague(), groupId);
    }

    private Optional<Long> resolveGroupId(Integer seasonId, Long userId) {
        return groupMemberRepository
                .findFirstBySeasonIdAndUserId(seasonId, userId)
                .map(member -> member.getGroup().getId());
    }
}
