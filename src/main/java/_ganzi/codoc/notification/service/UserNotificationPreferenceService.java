package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.UserNotificationPreference;
import _ganzi.codoc.notification.dto.NotificationPreferenceItem;
import _ganzi.codoc.notification.dto.NotificationPreferenceResponse;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.repository.UserNotificationPreferenceRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserNotificationPreferenceService {

    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final UserRepository userRepository;

    public NotificationPreferenceResponse getPreferences(Long userId) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        List<UserNotificationPreference> preferences =
                userNotificationPreferenceRepository.findAllByUserId(userId);

        return toPreferenceResponse(preferences);
    }

    @Transactional
    public void updatePreference(Long userId, NotificationType type, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        UserNotificationPreference preference =
                userNotificationPreferenceRepository
                        .findByUserIdAndType(userId, type)
                        .orElseGet(
                                () ->
                                        userNotificationPreferenceRepository.save(
                                                UserNotificationPreference.create(user, type, enabled)));

        preference.updateEnabled(enabled);
    }

    private NotificationPreferenceResponse toPreferenceResponse(
            List<UserNotificationPreference> preferences) {

        Map<NotificationType, Boolean> enabledByType = new EnumMap<>(NotificationType.class);

        for (UserNotificationPreference preference : preferences) {
            enabledByType.put(preference.getType(), preference.isEnabled());
        }

        return new NotificationPreferenceResponse(
                Arrays.stream(NotificationType.values())
                        .map(
                                type ->
                                        new NotificationPreferenceItem(type, enabledByType.getOrDefault(type, true)))
                        .toList());
    }
}
