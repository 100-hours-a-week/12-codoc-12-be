package _ganzi.codoc.user.service;

import _ganzi.codoc.user.domain.DailySolvedCount;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserStats;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.DailySolvedCountRepository;
import _ganzi.codoc.user.repository.UserRepository;
import _ganzi.codoc.user.repository.UserStatsRepository;
import _ganzi.codoc.user.service.dto.UserContributionResponse;
import _ganzi.codoc.user.service.dto.UserStatsResponse;
import _ganzi.codoc.user.service.dto.UserStreakResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsService {

    private static final int MIN_SOLVE_COUNT = 1;

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final DailySolvedCountRepository dailySolvedCountRepository;

    public UserStatsResponse getUserStats(Long userId) {
        UserStats userStats =
                userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return new UserStatsResponse(
                userStats.getXp(), userStats.getSolvedCount(), userStats.getSolvingCount());
    }

    public UserStreakResponse getUserStreak(Long userId) {
        UserStats userStats =
                userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return new UserStreakResponse(userStats.getStreak());
    }

    public UserContributionResponse getUserContribution(
            Long userId, LocalDate fromDate, LocalDate toDate) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<DailySolvedCount> dailySolvedCounts =
                dailySolvedCountRepository
                        .findAllByUserAndDateBetweenAndSolvedCountGreaterThanOrderByDateAsc(
                                user, fromDate, toDate, MIN_SOLVE_COUNT);
        List<UserContributionResponse.DailySolveCount> response =
                dailySolvedCounts.stream()
                        .map(
                                dailySolvedCount ->
                                        new UserContributionResponse.DailySolveCount(
                                                dailySolvedCount.getDate(), dailySolvedCount.getSolvedCount()))
                        .toList();
        return new UserContributionResponse(response);
    }

    @Transactional
    public void incrementSolvingCount(Long userId) {
        UserStats userStats =
                userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userStats.increaseSolvingCount();
    }

    @Transactional
    public void applyProblemSolved(Long userId, int xpAmount) {
        UserStats userStats =
                userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userStats.applyProblemSolved(xpAmount);
    }
}
