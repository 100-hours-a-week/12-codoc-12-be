package _ganzi.codoc.user.service;

import _ganzi.codoc.user.domain.*;
import _ganzi.codoc.user.enums.QuestStatus;
import _ganzi.codoc.user.exception.*;
import _ganzi.codoc.user.repository.DailySolvedCountRepository;
import _ganzi.codoc.user.repository.UserQuestRepository;
import _ganzi.codoc.user.repository.UserRepository;
import _ganzi.codoc.user.repository.UserStatsRepository;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

    private static final int WEEKLY_XP_PLACEHOLDER = 0; // 리더보드 추가 이전 사용하는 dummy

    private final UserRepository userRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserStatsRepository userStatsRepository;
    private final DailySolvedCountRepository dailySolvedCountRepository;
    private final JsonMapper jsonMapper;

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
            if (userQuest.getStatus() == QuestStatus.IN_PROGRESS && isQuestCompleted(userQuest)) {
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

    private boolean isQuestCompleted(UserQuest userQuest) {
        JsonNode requirements = parseRequirements(userQuest.getQuest().getRequirements());
        if (requirements == null || !requirements.isObject()) {
            return false;
        }

        if (requirements.has("DailySolvedCount")) {
            int required = requirements.path("DailySolvedCount").asInt(0);
            if (required <= 0 || !hasDailySolvedCount(userQuest.getUser(), required)) {
                return false;
            }
        }

        return true;
    }

    private JsonNode parseRequirements(String requirementsJson) {
        try {
            return jsonMapper.readTree(requirementsJson);
        } catch (Exception exception) {
            return null;
        }
    }

    private boolean hasDailySolvedCount(User user, int required) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        int solvedCount =
                dailySolvedCountRepository
                        .findByUserAndDate(user, today)
                        .map(DailySolvedCount::getSolvedCount)
                        .orElse(0);
        return solvedCount >= required;
    }
}
