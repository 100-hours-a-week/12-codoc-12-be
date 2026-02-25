package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroup;
import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardScoreId;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.domain.League;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupMemberRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardScoreRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.leaderboard.repository.LeagueRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaderboardSeasonBatchService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int GROUP_CAP = 30;

    private final LeaderboardSeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final LeaderboardGroupRepository groupRepository;
    private final LeaderboardGroupMemberRepository groupMemberRepository;
    private final LeaderboardScoreRepository scoreRepository;

    @Transactional
    public void assignGroupsForNextSeason() {
        SeasonWindow seasonWindow = computeNextSeasonWindow(ZonedDateTime.now(SEOUL));
        if (seasonRepository.existsById(seasonWindow.seasonId())) {
            return;
        }
        LeaderboardSeason season =
                LeaderboardSeason.create(
                        seasonWindow.seasonId(), seasonWindow.startsAt(), seasonWindow.endsAt());
        seasonRepository.save(season);

        List<League> leagues = leagueRepository.findAllByIsActiveTrueOrderBySortOrderAsc();
        List<LeaderboardScore> scores = new ArrayList<>();
        for (League league : leagues) {
            List<User> users =
                    userRepository.findAllByStatusAndLeagueId(UserStatus.ACTIVE, league.getId());
            if (users.isEmpty()) {
                continue;
            }
            List<User> shuffled = new ArrayList<>(users);
            Collections.shuffle(shuffled, new Random(computeSeed(seasonWindow.seasonId(), league)));
            int groupCount = (int) Math.ceil((double) shuffled.size() / GROUP_CAP);
            int baseSize = shuffled.size() / groupCount;
            int remainder = shuffled.size() % groupCount;
            int offset = 0;
            for (int index = 0; index < groupCount; index++) {
                int groupSize = baseSize + (index < remainder ? 1 : 0);
                LeaderboardGroup group =
                        groupRepository.save(LeaderboardGroup.create(season, league, index + 1));
                List<LeaderboardGroupMember> members = new ArrayList<>();
                for (int i = 0; i < groupSize; i++) {
                    User user = shuffled.get(offset + i);
                    members.add(LeaderboardGroupMember.create(group, seasonWindow.seasonId(), user));
                    scores.add(
                            LeaderboardScore.create(
                                    new LeaderboardScoreId(seasonWindow.seasonId(), user.getId()),
                                    user,
                                    league,
                                    group.getId()));
                }
                groupMemberRepository.saveAll(members);
                offset += groupSize;
            }
        }
        if (!scores.isEmpty()) {
            scoreRepository.saveAll(scores);
        }
    }

    private long computeSeed(int seasonId, League league) {
        return (((long) seasonId) << 32) ^ league.getId();
    }

    private SeasonWindow computeNextSeasonWindow(ZonedDateTime now) {
        ZonedDateTime candidateStart =
                now.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                        .withHour(1)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        if (now.isAfter(candidateStart)) {
            candidateStart =
                    now.with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                            .withHour(1)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0);
        }
        LocalDate startDate = candidateStart.toLocalDate();
        LocalDate endDate = startDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        ZonedDateTime endAt = endDate.atStartOfDay(SEOUL);
        WeekFields weekFields = WeekFields.ISO;
        int weekBasedYear = startDate.get(weekFields.weekBasedYear());
        int weekOfYear = startDate.get(weekFields.weekOfWeekBasedYear());
        int seasonId = (weekBasedYear * 100) + weekOfYear;
        return new SeasonWindow(seasonId, candidateStart.toInstant(), endAt.toInstant());
    }

    private record SeasonWindow(int seasonId, Instant startsAt, Instant endsAt) {}
}
