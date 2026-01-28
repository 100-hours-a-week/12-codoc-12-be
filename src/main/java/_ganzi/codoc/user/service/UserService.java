package _ganzi.codoc.user.service;

import _ganzi.codoc.auth.domain.SocialLogin;
import _ganzi.codoc.auth.repository.RefreshTokenRepository;
import _ganzi.codoc.auth.repository.SocialLoginRepository;
import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.domain.Avatar;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserStats;
import _ganzi.codoc.user.enums.DailyGoal;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.exception.AvatarNotFoundException;
import _ganzi.codoc.user.exception.DuplicateNicknameException;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.AvatarRepository;
import _ganzi.codoc.user.repository.UserRepository;
import _ganzi.codoc.user.repository.UserStatsRepository;
import _ganzi.codoc.user.service.dto.UserAvatarListResponse;
import _ganzi.codoc.user.service.dto.UserProfileResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final AvatarRepository avatarRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final QuestBatchService questBatchService;

    private static final int RANDOM_NICKNAME_LENGTH = 15;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public UserProfileResponse getUserProfile(Long id) {
        User user = getUser(id);
        Avatar avatar = user.getAvatar();
        Integer avatarId = avatar.getId();
        String avatarName = avatar.getName();
        String avatarImageUrl = avatar.getImageUrl();
        return new UserProfileResponse(user.getNickname(), avatarId, avatarName, avatarImageUrl);
    }

    public UserAvatarListResponse getAvatarList() {
        List<UserAvatarListResponse.AvatarItem> avatars =
                avatarRepository.findAll().stream()
                        .map(
                                avatar ->
                                        new UserAvatarListResponse.AvatarItem(avatar.getId(), avatar.getImageUrl()))
                        .toList();
        return new UserAvatarListResponse(avatars);
    }

    @Transactional
    public User createOnboardingUser() {
        Avatar defaultAvatar =
                avatarRepository.findByIsDefaultTrue().orElseThrow(AvatarNotFoundException::new);
        String nickname = generateUniqueNickname();
        User user = User.createOnboardingUser(nickname, defaultAvatar);
        UserStats userStats = UserStats.create(user);
        userStatsRepository.save(userStats);
        return userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long id, String nickname, Integer avatarId) {
        User user = getUser(id);
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(nickname, id)) {
                throw new DuplicateNicknameException();
            }
            user.updateNickname(nickname);
        }
        if (avatarId != null) {
            Avatar avatar = avatarRepository.findById(avatarId).orElseThrow(AvatarNotFoundException::new);
            user.updateAvatar(avatar);
        }
    }

    @Transactional
    public void updateDailyGoal(Long id, DailyGoal dailyGoal) {
        User user = getUser(id);
        user.updateDailyGoal(dailyGoal);
    }

    @Transactional
    public void markDormantUser(Long id) {
        User user = getUser(id);
        user.markDormant();
    }

    @Transactional
    public void reviveDormantUser(Long id) {
        User user = getUser(id);
        user.reviveFromDormant();
        if (user.getStatus() == UserStatus.ACTIVE) {
            questBatchService.issueDailyQuestsForUser(user.getId(), LocalDate.now(SEOUL));
        }
    }

    @Transactional
    public void completeOnboarding(Long id, UserInitSurveyRequest request) {
        User user = getUser(id);
        user.completeOnboarding(request.initLevel(), request.dailyGoal());
        if (user.getStatus() == UserStatus.ACTIVE) {
            questBatchService.issueDailyQuestsForUser(user.getId(), LocalDate.now(SEOUL));
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        if (user.getStatus() == UserStatus.ONBOARDING) {
            refreshTokenRepository.deleteByUser(user);
            socialLoginRepository.deleteByUser(user);
            userRepository.delete(user);
            return;
        }
        if (user.getStatus() == UserStatus.DELETED) {
            return;
        }
        user.markDeleted();
        refreshTokenRepository.deleteByUser(user);
        for (SocialLogin socialLogin : socialLoginRepository.findAllByUser(user)) {
            socialLogin.markDeleted();
        }
    }

    private String generateUniqueNickname() {
        String nickname;
        do {
            nickname = UUID.randomUUID().toString().replace("-", "");
            nickname = nickname.substring(0, RANDOM_NICKNAME_LENGTH);
        } while (userRepository.existsByNickname(nickname));
        return nickname;
    }
}
