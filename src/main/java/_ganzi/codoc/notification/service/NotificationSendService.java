package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.Notification;
import _ganzi.codoc.notification.domain.UserNotificationPreference;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.enums.PushNotificationSendResult;
import _ganzi.codoc.notification.repository.NotificationRepository;
import _ganzi.codoc.notification.repository.UserDeviceRepository;
import _ganzi.codoc.notification.repository.UserNotificationPreferenceRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationSendService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final NotificationRepository notificationRepository;
    private final PushNotificationSender pushNotificationSender;

    @Transactional
    public void send(Long userId, NotificationMessageItem messageItem) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (messageItem.type().isInboxVisible()) {
            notificationRepository.save(
                    Notification.create(
                            user,
                            messageItem.type(),
                            messageItem.title(),
                            messageItem.body(),
                            messageItem.type().getLinkCode(),
                            messageItem.linkParams()));
        }

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
