package _ganzi.codoc.chat.event;

import _ganzi.codoc.chat.dto.ChatUnreadStatusBroadcast;
import _ganzi.codoc.chat.service.ChatRelayService;
import _ganzi.codoc.chat.service.ChatUnreadCountService;
import _ganzi.codoc.chat.service.SharedWebSocketStateService;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ChatMessageCommitEventListener {

    private final ChatRelayService chatRelayService;
    private final ChatUnreadCountService chatUnreadCountService;
    private final SharedWebSocketStateService sharedWebSocketStateService;
    private final NotificationDispatchService notificationDispatchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageCommitted(ChatMessageCommittedEvent event) {
        chatRelayService.relayRoomMessage(event.roomId(), event.roomMessage());

        for (Long participantUserId : event.participantUserIds()) {
            boolean isConnected = sharedWebSocketStateService.isConnected(participantUserId);
            long totalUnreadCount =
                    chatUnreadCountService.increaseTotalUnreadCount(participantUserId, 1L, isConnected);
            if (isConnected && totalUnreadCount >= 0) {
                chatRelayService.relayUnreadStatusUpdate(
                        participantUserId, ChatUnreadStatusBroadcast.of(totalUnreadCount));
            }

            boolean isRoomSubscriber = event.roomSubscriberUserIds().contains(participantUserId);
            if (isRoomSubscriber) {
                continue;
            }

            if (isConnected) {
                chatRelayService.relayRoomUpdate(participantUserId, event.roomUpdate());
            }

            notificationDispatchService.dispatch(
                    participantUserId,
                    new NotificationMessageItem(
                            NotificationType.CHAT,
                            event.senderNickname(),
                            event.roomUpdate().lastMessagePreview(),
                            Map.of("roomId", String.valueOf(event.roomId()))));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatSystemMessageCommitted(ChatSystemMessageCommittedEvent event) {
        chatRelayService.relayRoomMessage(event.roomId(), event.roomMessage());
    }
}
