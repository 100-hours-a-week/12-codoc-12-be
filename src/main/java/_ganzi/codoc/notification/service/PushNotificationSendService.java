package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.UserNotificationPreference;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.enums.PushNotificationSendResult;
import _ganzi.codoc.notification.repository.UserDeviceRepository;
import _ganzi.codoc.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PushNotificationSendService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final PushNotificationSender pushNotificationSender;

    @Transactional
    public void send(Long userId, NotificationMessageItem messageItem) {

        if (!isPreferenceEnabled(userId, messageItem.type())) {
            return;
        }

        userDeviceRepository
                .findByUserIdAndActiveTrue(userId)
                .ifPresent(
                        userDevice -> {
                            PushNotificationSendResult sendResult =
                                    pushNotificationSender.send(messageItem, userDevice.getPushToken());
                            if (sendResult == PushNotificationSendResult.INVALID_TOKEN) {
                                userDevice.deactivate();
                            }
                        });
    }

    private boolean isPreferenceEnabled(Long userId, NotificationType type) {
        return userNotificationPreferenceRepository
                .findByUserIdAndType(userId, type)
                .map(UserNotificationPreference::isEnabled)
                .orElse(true);
    }
}
