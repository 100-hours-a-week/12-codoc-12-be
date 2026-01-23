package _ganzi.codoc.user.service;

import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.domain.Avatar;
import _ganzi.codoc.user.domain.AvatarRepository;
import _ganzi.codoc.user.domain.DailyGoal;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.domain.UserRepository;
import _ganzi.codoc.user.exception.DuplicateNicknameException;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.service.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AvatarRepository avatarRepository;

    private static final int DEFAULT_AVATAR_ID = 1;

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

    @Transactional
    public User createOnboardingUser(String nickname) {
        Avatar defaultAvatar =
                avatarRepository.findById(DEFAULT_AVATAR_ID).orElseThrow(UserNotFoundException::new);
        User user = User.createOnboardingUser(nickname, defaultAvatar);
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
            Avatar avatar = avatarRepository.findById(avatarId).orElseThrow(UserNotFoundException::new);
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
    }

    @Transactional
    public void completeOnboarding(Long id, UserInitSurveyRequest request) {
        User user = getUser(id);
        user.completeOnboarding(request.initLevel(), request.dailyGoal());
    }
}
