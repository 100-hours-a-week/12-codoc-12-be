package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.Notification;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

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
    }
}
