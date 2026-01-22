package _ganzi.codoc.user.service;

import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.domain.Avatar;
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

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public UserProfileResponse getUserProfile(Long id) {
        User user = getUser(id);
        Avatar avatar = user.getAvatar();
        Integer avatarId = avatar != null ? avatar.getId() : null;
        String avatarName = avatar != null ? avatar.getName() : null;
        String avatarImageUrl = avatar != null ? avatar.getImageUrl() : null;
        return new UserProfileResponse(user.getNickname(), avatarId, avatarName, avatarImageUrl);
    }

    @Transactional
    public void changeNickname(Long id, String nickname) {
        User user = getUser(id);
        if (nickname.equals(user.getNickname())) {
            return;
        }
        if (userRepository.existsByNicknameAndIdNot(nickname, id)) {
            throw new DuplicateNicknameException();
        }
        user.updateNickname(nickname);
    }

    @Transactional
    public void updateProfile(Long id, String nickname, Avatar avatar) {
        User user = getUser(id);
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(nickname, id)) {
                throw new DuplicateNicknameException();
            }
            user.updateNickname(nickname);
        }
        if (avatar != null) {
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
    public void saveInitSurvey(Long id, UserInitSurveyRequest request) {
        User user = getUser(id);
        user.completeOnboarding(request.initLevel(), request.dailyGoal());
    }
}
