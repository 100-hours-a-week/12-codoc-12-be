package _ganzi.codoc.user.service;

import _ganzi.codoc.user.domain.Quest;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserQuest;
import _ganzi.codoc.user.domain.UserStats;
import _ganzi.codoc.user.enums.QuestStatus;
import _ganzi.codoc.user.exception.*;
import _ganzi.codoc.user.repository.UserQuestRepository;
import _ganzi.codoc.user.repository.UserRepository;
import _ganzi.codoc.user.repository.UserStatsRepository;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import _ganzi.codoc.user.service.requirements.QuestRequirementRegistry;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

    private static final int WEEKLY_XP_PLACEHOLDER = 0; // 리더보드 추가 이전 사용하는 dummy

    private final UserRepository userRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserStatsRepository userStatsRepository;
    private final QuestRequirementRegistry questRequirementRegistry;

    public UserQuestListResponse getUserQuests(Long userId) {
        User user = getUser(userId);
        List<UserQuest> userQuests = userQuestRepository.findAllByUserAndIsExpiredFalseFetchQuest(user);
        List<UserQuestListResponse.UserQuestSummary> quests =
                userQuests.stream()
                        .map(
                                userQuest ->
                                        new UserQuestListResponse.UserQuestSummary(
                                                userQuest.getId(),
                                                userQuest.getQuest().getTitle(),
                                                userQuest.getQuest().getReward(),
                                                userQuest.getQuest().getType(),
                                                userQuest.getStatus()))
                        .toList();
        return new UserQuestListResponse(quests);
    }

    @Transactional
    public QuestRewardResponse claimReward(Long userId, Long userQuestId) {
        User user = getUser(userId);
        UserQuest userQuest =
                userQuestRepository
                        .findByIdAndUser(userQuestId, user)
                        .orElseThrow(QuestNotFoundException::new);
        validateClaimable(userQuest);

        Quest quest = userQuest.getQuest();
        UserStats userStats =
                userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userStats.addXp(quest.getReward());
        userQuest.markClaimed();

        return new QuestRewardResponse(userStats.getXp(), WEEKLY_XP_PLACEHOLDER, userQuest.getId());
    }

    @Transactional
    public void refreshUserQuestStatuses(Long userId) {
        User user = getUser(userId);
        List<UserQuest> userQuests = userQuestRepository.findAllByUserAndIsExpiredFalseFetchQuest(user);
        Instant now = Instant.now();
        for (UserQuest userQuest : userQuests) {
            if (userQuest.getExpiresAt().isBefore(now)) {
                userQuest.markExpired();
                continue;
            }
            if (userQuest.getStatus() == QuestStatus.IN_PROGRESS
                    && questRequirementRegistry.isSatisfied(userQuest)) {
                userQuest.markCompleted();
            }
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void validateClaimable(UserQuest userQuest) {
        if (userQuest.isExpired() || userQuest.getExpiresAt().isBefore(Instant.now())) {
            userQuest.markExpired();
            throw new QuestExpiredException();
        }
        if (userQuest.getStatus() == QuestStatus.CLAIMED) {
            throw new QuestAlreadyClaimedException();
        }
        if (userQuest.getStatus() != QuestStatus.COMPLETED) {
            throw new QuestInProgressException();
        }
    }
}
