package _ganzi.codoc.user.service;

import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.domain.*;
import _ganzi.codoc.user.exception.AvatarNotFoundException;
import _ganzi.codoc.user.exception.DuplicateNicknameException;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.service.dto.UserProfileResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AvatarRepository avatarRepository;

    private static final int RANDOM_NICKNAME_LENGTH = 15;

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
    public User createOnboardingUser() {
        Avatar defaultAvatar =
                avatarRepository.findByIsDefaultTrue().orElseThrow(AvatarNotFoundException::new);
        String nickname = generateUniqueNickname();
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
    }

    @Transactional
    public void completeOnboarding(Long id, UserInitSurveyRequest request) {
        User user = getUser(id);
        user.completeOnboarding(request.initLevel(), request.dailyGoal());
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
